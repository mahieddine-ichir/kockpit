package com.accor.wcp.console.services.sqsdlq.dynamo;

import static com.accor.wcp.console.services.sqsdlq.DynamoDbProcessor.WCP_RETRY_MESSAGE_ID;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.accor.wcp.console.services.sqsdlq.NotFoundException;
import com.accor.wcp.console.services.sqsdlq.SqsDlqSettingsService;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsAttributeType;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentRetry;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentStatus;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2;
import com.accor.wcp.console.services.sqsdlq.model.Count;
import com.accor.wcp.console.services.sqsdlq.model.PartitionKey;
import com.accor.wcp.console.services.sqsdlq.model.RetryDto;
import com.accor.wcp.console.services.sqsdlq.model.SqsDlqSettingsDto;
import com.accor.wcp.console.services.sqsdlq.model.SqsMessageDtoV2;
import com.accor.wcp.console.services.sqsdlq.model.SqsMessagesDtoV2;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.CollectionUtils;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@ExtendWith(MockitoExtension.class)
class SqsDocumentServiceV2Test {
  @InjectMocks private SqsDocumentServiceV2 underTest;

  @Mock private SqsDocumentRepositoryV2 sqsDocumentRepositoryV2;

  @Mock private SqsDlqSettingsService sqsDlqSettingsService;

  @Test
  void find_nominal_when_lastSortKey_is_null() {
    // GIVEN
    PartitionKey partitionKey =
        PartitionKey.builder()
            .domain("mock_domain")
            .env("mock_env")
            .queueName("mock_queueName")
            .build();
    String lastSortKey = null;
    String oneStatus = "NEW";
    List<String> status = List.of(oneStatus);
    String body = "mock_body";

    SqsMessageDtoV2 sqsMessageDto =
        SqsMessageDtoV2.builder()
            .partitionKey(partitionKey.toString())
            .body(body)
            .status(SqsDocumentStatus.NEW)
            .build();

    SqsDocumentV2 sqsDocument =
        SqsDocumentV2.builder().partitionKey(partitionKey.toString()).body(body).build();
    doReturn(List.of(sqsDocument))
        .when(sqsDocumentRepositoryV2)
        .fetchFirstPage(eq(partitionKey), eq(mockPostFilter(status)));

    // WHEN
    SqsMessagesDtoV2 sqsMessagesDtoV2 = underTest.find(partitionKey, lastSortKey, status);

    // THEN
    verify(sqsDocumentRepositoryV2, never()).fetchNextPage(any(), any(), any());
    verify(sqsDocumentRepositoryV2).fetchFirstPage(eq(partitionKey), eq(mockPostFilter(status)));
    assertThat(sqsMessagesDtoV2.getNbMessagesByStatus())
        .isEqualTo(Map.of(SqsDocumentStatus.NEW, 1L));
    assertThat(sqsMessagesDtoV2.getMessages()).isEqualTo(List.of(sqsMessageDto));
  }

  @Test
  void find_nominal_when_lastSortKey_is_NOT_null() {
    // GIVEN
    PartitionKey partitionKey =
        PartitionKey.builder()
            .domain("mock_domain")
            .env("mock_env")
            .queueName("mock_queueName")
            .build();
    String lastSortKey = "mock_lastSortKey";
    String oneStatus = "NEW";
    List<String> status = List.of(oneStatus);
    String body = "mock_body";

    SqsMessageDtoV2 sqsMessageDto =
        SqsMessageDtoV2.builder()
            .partitionKey(partitionKey.toString())
            .body(body)
            .status(SqsDocumentStatus.NEW)
            .build();

    SqsDocumentV2 sqsDocument =
        SqsDocumentV2.builder().partitionKey(partitionKey.toString()).body(body).build();
    doReturn(List.of(sqsDocument))
        .when(sqsDocumentRepositoryV2)
        .fetchNextPage(eq(partitionKey), eq(lastSortKey), eq(mockPostFilter(status)));

    // WHEN
    SqsMessagesDtoV2 sqsMessagesDtoV2 = underTest.find(partitionKey, lastSortKey, status);

    // THEN
    verify(sqsDocumentRepositoryV2)
        .fetchNextPage(eq(partitionKey), eq(lastSortKey), eq(mockPostFilter(status)));
    verify(sqsDocumentRepositoryV2, never()).fetchFirstPage(any(), any());
    assertThat(sqsMessagesDtoV2.getNbMessagesByStatus())
        .isEqualTo(Map.of(SqsDocumentStatus.NEW, 1L));
    assertThat(sqsMessagesDtoV2.getMessages()).isEqualTo(List.of(sqsMessageDto));
  }

  @Test
  void fetchOne_nominal() {
    // GIVEN
    PartitionKey partitionKey =
        PartitionKey.builder()
            .domain("mock_domain")
            .env("mock_env")
            .queueName("mock_queueName")
            .build();
    String id = "mock_id";
    String body = "mock_body";

    SqsDocumentV2 sqsDocument =
        SqsDocumentV2.builder().partitionKey(partitionKey.toString()).id(id).body(body).build();
    doReturn(Optional.of(sqsDocument))
        .when(sqsDocumentRepositoryV2)
        .fetchOne(eq(partitionKey), eq(id));

    // WHEN
    SqsMessageDtoV2 sqsMessageDto = underTest.fetchOne(partitionKey, id);

    // THEN
    verify(sqsDocumentRepositoryV2).fetchOne(eq(partitionKey), eq(id));
    assertThat(sqsMessageDto)
        .matches(m -> m.getPartitionKey().equals(partitionKey.toString()))
        .matches(m -> m.getId().equals(id))
        .matches(m -> m.getBody().equals(body));
  }

  @Test
  void fetchOne_error_when_not_found() {
    // GIVEN
    PartitionKey partitionKey =
        PartitionKey.builder()
            .domain("mock_domain")
            .env("mock_env")
            .queueName("mock_queueName")
            .build();
    String id = "mock_id";
    doReturn(Optional.empty()).when(sqsDocumentRepositoryV2).fetchOne(eq(partitionKey), eq(id));

    // WHEN / THEN
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> underTest.fetchOne(partitionKey, id))
        .withMessage(
            "Message not found for partitionKey: '" + partitionKey + "' and id: '" + id + "'");
  }

  @Test
  void update_nominal() {
    // GIVEN
    PartitionKey partitionKey =
        PartitionKey.builder()
            .domain("mock_domain")
            .env("mock_env")
            .queueName("mock_queueName")
            .build();
    String id = "mock_id";
    String body = "mock_body";

    SqsDocumentV2 sqsDocument =
        SqsDocumentV2.builder().partitionKey(partitionKey.toString()).id(id).body(body).build();
    doReturn(Optional.of(sqsDocument))
        .when(sqsDocumentRepositoryV2)
        .fetchOne(eq(partitionKey), eq(id));
    doReturn(SqsDlqSettingsDto.builder().build())
        .when(sqsDlqSettingsService)
        .getSqsDlqSettingsDto(eq(partitionKey));
    SqsMessageDtoV2 message =
        SqsMessageDtoV2.builder()
            .partitionKey(partitionKey.toString())
            .id(id)
            .body(body)
            .status(SqsDocumentStatus.NEW)
            .build();

    // WHEN
    underTest.update(partitionKey, message);

    // THEN
    verify(sqsDocumentRepositoryV2).fetchOne(eq(partitionKey), eq(id));
    verify(sqsDlqSettingsService).getSqsDlqSettingsDto(eq(partitionKey));
    verify(sqsDocumentRepositoryV2).update(eq(sqsDocument));
  }

  @Test
  void update_error_when_not_found() {
    // GIVEN
    PartitionKey partitionKey =
        PartitionKey.builder()
            .domain("mock_domain")
            .env("mock_env")
            .queueName("mock_queueName")
            .build();
    String id = "mock_id";
    String body = "mock_body";

    doReturn(Optional.empty()).when(sqsDocumentRepositoryV2).fetchOne(eq(partitionKey), eq(id));
    SqsMessageDtoV2 message =
        SqsMessageDtoV2.builder()
            .partitionKey(partitionKey.toString())
            .id(id)
            .body(body)
            .status(SqsDocumentStatus.NEW)
            .build();

    // WHEN / THEN
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> underTest.update(partitionKey, message))
        .withMessage(
            "Message not found for partitionKey: '" + partitionKey + "' and id: '" + id + "'");
  }

  @Test
  void addIdRetryAttributeAndSaveRetry_nominal() {
    // GIVEN
    PartitionKey partitionKey =
        PartitionKey.builder()
            .domain("mock_domain")
            .env("mock_env")
            .queueName("mock_queueName")
            .build();
    String id = "mock_id";
    String body = "mock_body";
    SqsDocumentRetry sqsDocumentRetryMock = SqsDocumentRetry.builder().build();
    RetryDto retry = RetryDto.builder().parentId(id).retry(sqsDocumentRetryMock).build();

    SqsDocumentV2 sqsDocument =
        SqsDocumentV2.builder().partitionKey(partitionKey.toString()).id(id).body(body).build();
    doReturn(Optional.of(sqsDocument))
        .when(sqsDocumentRepositoryV2)
        .fetchOne(eq(partitionKey), eq(id));

    // WHEN
    SqsDocumentRetry sqsDocumentRetry =
        underTest.addIdRetryAttributeAndSaveRetry(partitionKey, retry);

    // THEN
    verify(sqsDocumentRepositoryV2).fetchOne(eq(partitionKey), eq(id));
    assertThat(sqsDocumentRetry).isEqualTo(sqsDocumentRetryMock);
    assertThat(sqsDocumentRetryMock.getAttributes())
        .hasSize(2)
        .anyMatch(m -> m.getName().equals(WCP_RETRY_MESSAGE_ID))
        .anyMatch(m -> m.getType().equals(SqsAttributeType.STRING))
        .anyMatch(m -> nonNull(m.getValue()));
  }

  @Test
  void addIdRetryAttributeAndSaveRetry_error_when_not_found() {
    // GIVEN
    PartitionKey partitionKey =
        PartitionKey.builder()
            .domain("mock_domain")
            .env("mock_env")
            .queueName("mock_queueName")
            .build();
    String id = "mock_id";
    String body = "mock_body";
    SqsDocumentRetry sqsDocumentRetryMock = SqsDocumentRetry.builder().build();
    RetryDto retry = RetryDto.builder().parentId(id).retry(sqsDocumentRetryMock).build();

    doReturn(Optional.empty()).when(sqsDocumentRepositoryV2).fetchOne(eq(partitionKey), eq(id));

    // WHEN / THEN
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> underTest.addIdRetryAttributeAndSaveRetry(partitionKey, retry))
        .withMessage(
            "Message not found for partitionKey: '" + partitionKey + "' and id: '" + id + "'");
  }

  @Test
  void delete_all_nominal() {
    // GIVEN
    PartitionKey partitionKey =
        PartitionKey.builder()
            .domain("mock_domain")
            .env("mock_env")
            .queueName("mock_queueName")
            .build();

    // WHEN
    underTest.delete(partitionKey, null);

    // THEN
    verify(sqsDocumentRepositoryV2, never()).deletePage(any());
    verify(sqsDocumentRepositoryV2).deleteAll(eq(partitionKey));
  }

  @Test
  void delete_multiple_nominal() {
    // GIVEN
    PartitionKey partitionKey =
        PartitionKey.builder()
            .domain("mock_domain")
            .env("mock_env")
            .queueName("mock_queueName")
            .build();

    // WHEN
    underTest.delete(partitionKey, List.of("mock_id"));

    // THEN
    verify(sqsDocumentRepositoryV2, never()).deleteAll(eq(partitionKey));
    verify(sqsDocumentRepositoryV2)
        .deletePage(
            eq(
                List.of(
                    Key.builder()
                        .partitionValue(partitionKey.toString())
                        .sortValue("mock_id")
                        .build())));
  }

  @Test
  void getCount_nominal() {
    // GIVEN
    PartitionKey partitionKey =
        PartitionKey.builder()
            .domain("mock_domain")
            .env("mock_env")
            .queueName("mock_queueName")
            .build();
    int result = 123;
    doReturn(result).when(sqsDocumentRepositoryV2).getCount(eq(partitionKey));

    // WHEN
    Count count = underTest.getCount(partitionKey);

    // THEN
    verify(sqsDocumentRepositoryV2).getCount(eq(partitionKey));
    assertThat(count).isEqualTo(Count.builder().totalCount(result).build());
  }

  private Optional<Expression> mockPostFilter(List<String> status) {
    if (CollectionUtils.isEmpty(status)) {
      return Optional.empty();
    }
    Map<String, AttributeValue> expressionAttributeValues =
        new HashMap<>(Map.of(":statusValues", AttributeValue.builder().ss(status).build()));
    String expressionQuery = "contains(:statusValues, #statusName)";
    Expression expression =
        Expression.builder()
            .expression(expressionQuery)
            .expressionValues(expressionAttributeValues)
            .expressionNames(Map.of("#statusName", "status"))
            .build();
    return Optional.of(expression);
  }
}
