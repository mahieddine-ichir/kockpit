package com.accor.wcp.console.services.sqsdlq.dynamo;

import static com.accor.wcp.console.services.sqsdlq.DynamoDbProcessor.WCP_RETRY_MESSAGE_ID;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.accor.wcp.console.services.sqsdlq.NotFoundException;
import com.accor.wcp.console.services.sqsdlq.SqsDlqSettingsService;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.AttributeDocument;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsAttributeType;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentRetry;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentStatus;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2;
import com.accor.wcp.console.services.sqsdlq.mapper.SqsMessageMapperV2;
import com.accor.wcp.console.services.sqsdlq.model.Count;
import com.accor.wcp.console.services.sqsdlq.model.PartitionKey;
import com.accor.wcp.console.services.sqsdlq.model.RetryDto;
import com.accor.wcp.console.services.sqsdlq.model.SqsDlqSettingsDto;
import com.accor.wcp.console.services.sqsdlq.model.SqsMessageDtoV2;
import com.accor.wcp.console.services.sqsdlq.model.SqsMessagesDtoV2;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Service
@AllArgsConstructor
public class SqsDocumentServiceV2 {
  private final SqsMessageMapperV2 sqsMessageMapperV2 = Mappers.getMapper(SqsMessageMapperV2.class);
  private final SqsDocumentRepositoryV2 sqsDocumentRepositoryV2;
  private final SqsDlqSettingsService sqsDlqSettingsService;

  public SqsMessagesDtoV2 find(PartitionKey partitionKey, String lastSortKey, List<String> status) {
    Optional<Expression> postFilter = postFilter(status);

    List<SqsDocumentV2> page =
        isNotBlank(lastSortKey)
            ? sqsDocumentRepositoryV2.fetchNextPage(partitionKey, lastSortKey, postFilter)
            : sqsDocumentRepositoryV2.fetchFirstPage(partitionKey, postFilter);

    List<SqsMessageDtoV2> pageDto = sqsMessageMapperV2.fromDocument(page);
    Map<SqsDocumentStatus, Long> nbMessagesByStatus =
        page.stream()
            .collect(Collectors.groupingBy(SqsDocumentV2::getStatus, Collectors.counting()));

    return SqsMessagesDtoV2.builder()
        .messages(pageDto)
        .nbMessagesByStatus(nbMessagesByStatus)
        .build();
  }

  private Optional<Expression> postFilter(List<String> status) {
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

  public SqsMessageDtoV2 fetchOne(PartitionKey partitionKey, String id) throws NotFoundException {
    Optional<SqsDocumentV2> sqsDocument = sqsDocumentRepositoryV2.fetchOne(partitionKey, id);
    if (sqsDocument.isEmpty()) {
      throw new NotFoundException(
          "Message not found for partitionKey: '" + partitionKey + "' and id: '" + id + "'");
    } else {
      return sqsMessageMapperV2.fromDocument(sqsDocument.get());
    }
  }

  public void update(PartitionKey partitionKey, SqsMessageDtoV2 message) throws NotFoundException {
    String id = message.getId();
    Optional<SqsDocumentV2> sqsDocument = sqsDocumentRepositoryV2.fetchOne(partitionKey, id);
    if (sqsDocument.isEmpty()) {
      throw new NotFoundException(
          "Message not found for partitionKey: '" + partitionKey + "' and id: '" + id + "'");
    } else {
      SqsDlqSettingsDto sqsDlqSettingsDto =
          sqsDlqSettingsService.getSqsDlqSettingsDto(partitionKey);
      sqsDocumentRepositoryV2.update(
          sqsMessageMapperV2.toDocument(message, sqsDlqSettingsDto.getTtl()));
    }
  }

  public SqsDocumentRetry addIdRetryAttributeAndSaveRetry(PartitionKey partitionKey, RetryDto retry)
      throws NotFoundException {
    String id = retry.getParentId();
    SqsDocumentRetry sqsDocumentRetry = retry.getRetry();
    Optional<SqsDocumentV2> sqsDocumentOpt = sqsDocumentRepositoryV2.fetchOne(partitionKey, id);
    if (sqsDocumentOpt.isEmpty()) {
      throw new NotFoundException(
          "Message not found for partitionKey: '" + partitionKey + "' and id: '" + id + "'");
    } else {
      sqsDocumentRetry
          .getAttributes()
          .add(
              AttributeDocument.builder()
                  .name(WCP_RETRY_MESSAGE_ID)
                  .type(SqsAttributeType.STRING)
                  .value(UUID.randomUUID().toString())
                  .build());

      SqsDocumentV2 sqsDocument = sqsDocumentOpt.get();
      sqsDocument.getRetries().add(sqsDocumentRetry);
      sqsDocumentRepositoryV2.update(sqsDocument);

      sqsDocumentRetry
          .getAttributes()
          .add(
              AttributeDocument.builder()
                  .name("WCP_RETRY_MESSAGE_PARENT_ID")
                  .type(SqsAttributeType.STRING)
                  .value(id)
                  .build());
    }
    return sqsDocumentRetry;
  }

  public void delete(PartitionKey partitionKey, List<String> sortKeys) {
    if (CollectionUtils.isEmpty(sortKeys)) {
      deleteAll(partitionKey);
    } else {
      deletePage(partitionKey, sortKeys);
    }
  }

  private void deletePage(PartitionKey partitionKey, List<String> sortKeys) {
    List<Key> keys =
        sortKeys.stream()
            .filter(Objects::nonNull)
            .map(sortKey -> extractKey(partitionKey, sortKey))
            .toList();
    sqsDocumentRepositoryV2.deletePage(keys);
  }

  private void deleteAll(PartitionKey partitionKey) {
    sqsDocumentRepositoryV2.deleteAll(partitionKey);
  }

  private Key extractKey(PartitionKey partitionKey, String sortKey) {
    return Key.builder().partitionValue(partitionKey.toString()).sortValue(sortKey).build();
  }

  public Count getCount(PartitionKey partitionKey) {
    return Count.builder().totalCount(sqsDocumentRepositoryV2.getCount(partitionKey)).build();
  }
}
