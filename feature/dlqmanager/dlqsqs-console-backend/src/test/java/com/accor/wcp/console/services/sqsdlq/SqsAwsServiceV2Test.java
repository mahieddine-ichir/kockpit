package com.accor.wcp.console.services.sqsdlq;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accor.wcp.console.services.sqsdlq.dynamo.SqsDocumentRepositoryV2;
import com.accor.wcp.console.services.sqsdlq.dynamo.SqsDocumentServiceV2;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentRetry;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2;
import com.accor.wcp.console.services.sqsdlq.model.PartitionKey;
import com.accor.wcp.console.services.sqsdlq.model.RetryDto;
import com.accor.wcp.console.services.sqsdlq.model.SqsDlqSettingsDto;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResultEntry;

@ExtendWith(MockitoExtension.class)
public class SqsAwsServiceV2Test {

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private SqsDocumentRepositoryV2 sqsDocumentRepositoryV2;

  private SqsDocumentServiceV2 sqsDocumentServiceV2;

  private SqsClient sqsClient;

  private SqsAwsServiceV2 underTest;

  @Captor private ArgumentCaptor<SendMessageBatchRequest> sendMessageBatchRequestCaptor;

  @Captor private ArgumentCaptor<List<String>> deleteArgumentCaptor;

  @Captor private ArgumentCaptor<RetryDto> addIdRetryAttributeAndSaveRetryCaptor;

  @BeforeEach
  void init(
      @Mock SqsClient sqsClient,
      @Mock SqsDocumentServiceV2 sqsDocumentServiceV2,
      @Mock SqsDocumentRepositoryV2 sqsDocumentRepositoryV2) {
    this.sqsClient = sqsClient;
    this.sqsDocumentServiceV2 = sqsDocumentServiceV2;
    this.sqsDocumentRepositoryV2 = sqsDocumentRepositoryV2;
    this.underTest =
        new SqsAwsServiceV2(sqsClient, sqsDocumentRepositoryV2, sqsDocumentServiceV2, 5);
    ReflectionTestUtils.setField(this.underTest, "clock", clock);
  }

  @Test
  public void sendAllRetryMessage_check_infinite_loop_for_bugged_application() {
    // GIVEN
    String domain = "wcc";
    String env = "test";
    String queueName = "test-dlq";
    PartitionKey partitionKey =
        PartitionKey.builder().domain(domain).env(env).queueName(queueName).build();
    SqsDlqSettingsDto settings = SqsDlqSettingsDto.builder().deleteWhenReplay(true).build();

    long sentTimestamp = Instant.now(clock).toEpochMilli() + 1;
    String id = String.format("%s_3c5242e3-dacc-8495-ec5d-613411ec40", sentTimestamp);
    SqsDocumentV2 entity =
        SqsDocumentV2.builder()
            .partitionKey(partitionKey.toString())
            .id(id)
            .body("{ \"pmid\" : \"mock_pmid\" }")
            .build();
    doReturn(List.of(entity))
        .when(sqsDocumentRepositoryV2)
        .fetchFirstPage(eq(partitionKey), eq(Optional.empty()));
    doReturn(List.of(entity))
        .when(sqsDocumentRepositoryV2)
        .fetchNextPage(eq(partitionKey), eq(id), eq(Optional.empty()));

    SendMessageBatchResponse response =
        SendMessageBatchResponse.builder()
            .successful(List.of(SendMessageBatchResultEntry.builder().id(id).build()))
            .build();
    doReturn(response).when(sqsClient).sendMessageBatch(any(SendMessageBatchRequest.class));

    // WHEN / THEN
    assertThatExceptionOfType(InfiniteLoopRiskException.class)
        .isThrownBy(() -> underTest.sendAllRetryMessage(partitionKey, settings))
        .withMessage(
            """
                New DLQ messages have been detected since 'replay all' has been launched.
                It could lead to an infinite loop for domain: 'wcc', env: 'test', queueName: 'test-dlq'.
                If your application is not infinitely resending the same DLQ messages, relaunch 'replay all'.
                """);
  }

  @Test
  public void should_send_one_messages_and_delete() {
    // GIVEN
    PartitionKey partitionKey =
        PartitionKey.builder().queueName("test-dlq").domain("wcc").env("test").build();
    SqsDlqSettingsDto settings = SqsDlqSettingsDto.builder().deleteWhenReplay(true).build();

    String parentId = "1649059165761_3c5242e3-dacc-8495-ec5d-613411ec4069";
    String body = "{ \"pmid\" : \"31\" }";
    List<RetryDto> retries =
        List.of(
            RetryDto.builder()
                .parentId(parentId)
                .retry(
                    SqsDocumentRetry.builder()
                        .attributes(Collections.emptyList())
                        .body(body)
                        .sentTimestamp("1649062682756")
                        .status("Sent")
                        .build())
                .build());

    when(sqsClient.sendMessageBatch(any(SendMessageBatchRequest.class)))
        .thenReturn(
            SendMessageBatchResponse.builder()
                .successful(List.of(SendMessageBatchResultEntry.builder().id(parentId).build()))
                .build());
    // WHEN
    underTest.sendMultipleRetryMessage(partitionKey, retries, settings);
    // THEN

    // should not save the retry as it will be deleted
    verify(sqsDocumentServiceV2, times(0)).addIdRetryAttributeAndSaveRetry(any(), any());
    verify(sqsDocumentServiceV2).delete(eq(partitionKey), eq(List.of(parentId)));
    verify(sqsClient).sendMessageBatch(sendMessageBatchRequestCaptor.capture());

    Assertions.assertThat(sendMessageBatchRequestCaptor.getAllValues().size())
        .as("The number of message sent is correct")
        .isEqualTo(1);
    Assertions.assertThat(
            sendMessageBatchRequestCaptor.getAllValues().get(0).entries().get(0).messageBody())
        .as("The good message(s) body has been sent")
        .isEqualTo(body);
  }

  public static Stream<Arguments> getArguments() {
    return Stream.of(
        Arguments.of(1), Arguments.of(3), Arguments.of(10), Arguments.of(25), Arguments.of(33));
  }

  @ParameterizedTest
  @MethodSource("getArguments")
  public void should_send_n_messages_and_delete(int endExclusive) {
    // GIVEN
    PartitionKey partitionKey =
        PartitionKey.builder().queueName("test-dlq").domain("wcc").env("test").build();
    SqsDlqSettingsDto settings = SqsDlqSettingsDto.builder().deleteWhenReplay(true).build();

    List<RetryDto> retries =
        IntStream.range(0, endExclusive)
            .mapToObj(Integer::valueOf)
            .map(
                i ->
                    RetryDto.builder()
                        .parentId(
                            String.format(
                                "1649059165761_3c5242e3-dacc-8495-ec5d-613411ec40%02d", i))
                        .retry(
                            SqsDocumentRetry.builder()
                                .attributes(Collections.emptyList())
                                .body(String.format("{ \"pmid\" : \"%02d\" }", i))
                                .sentTimestamp("1649062682756")
                                .status("Sent")
                                .build())
                        .build())
            .toList();

    when(sqsClient.sendMessageBatch(any(SendMessageBatchRequest.class)))
        .thenAnswer(
            invocation ->
                SendMessageBatchResponse.builder()
                    .successful(
                        invocation.getArgument(0, SendMessageBatchRequest.class).entries().stream()
                            .map(
                                entry ->
                                    SendMessageBatchResultEntry.builder().id(entry.id()).build())
                            .toList())
                    .build());
    // WHEN
    underTest.sendMultipleRetryMessage(partitionKey, retries, settings);
    // THEN

    // Assert that services are called
    verify(sqsDocumentServiceV2, times(0)).addIdRetryAttributeAndSaveRetry(any(), any());
    verify(sqsClient, times((int) Math.ceil(Double.valueOf(endExclusive) / 10D)))
        .sendMessageBatch(sendMessageBatchRequestCaptor.capture());
    verify(sqsDocumentServiceV2, times((int) Math.ceil(Double.valueOf(endExclusive) / 10D)))
        .delete(eq(partitionKey), deleteArgumentCaptor.capture());

    Assertions.assertThat(
            sendMessageBatchRequestCaptor.getAllValues().stream()
                .map(SendMessageBatchRequest::entries)
                .flatMap(Collection::stream)
                .toList()
                .size())
        .as("The number of message sent is correct")
        .isEqualTo(endExclusive);
    Assertions.assertThat(
            sendMessageBatchRequestCaptor.getAllValues().stream()
                .map(SendMessageBatchRequest::entries)
                .flatMap(Collection::stream)
                .toList())
        .as("The good message(s) body has been sent")
        .allMatch(
            sendMessageBatchRequestEntry ->
                sendMessageBatchRequestEntry
                    .messageBody()
                    .equals(
                        String.format(
                            "{ \"pmid\" : \"%s\" }",
                            sendMessageBatchRequestEntry
                                .id()
                                .substring(sendMessageBatchRequestEntry.id().length() - 2))));
    Assertions.assertThat(
            deleteArgumentCaptor.getAllValues().stream().flatMap(Collection::stream).toList())
        .as("The message has been deleted")
        .containsExactlyInAnyOrderElementsOf(retries.stream().map(RetryDto::getParentId).toList());
  }

  @Test
  public void should_send_replay_and_delete_all_messages() {
    // GIVEN
    PartitionKey partitionKey =
        PartitionKey.builder().queueName("test-dlq").domain("wcc").env("test").build();
    SqsDlqSettingsDto settings = SqsDlqSettingsDto.builder().deleteWhenReplay(true).build();
    List<SqsDocumentV2> documents =
        IntStream.range(0, 43)
            .mapToObj(Integer::valueOf)
            .map(
                i ->
                    SqsDocumentV2.builder()
                        .id(
                            String.format(
                                "1649059165761_3c5242e3-dacc-8495-ec5d-613411ec40%02d", i))
                        .body(String.format("{ \"pmid\" : \"%02d\" }", i))
                        .build())
            .toList();

    // Assuming that we are paging by TestPagination
    mockRepostitory(partitionKey, documents, 10);

    when(sqsClient.sendMessageBatch(any(SendMessageBatchRequest.class)))
        .thenAnswer(
            invocation ->
                SendMessageBatchResponse.builder()
                    .successful(
                        invocation.getArgument(0, SendMessageBatchRequest.class).entries().stream()
                            .map(
                                entry ->
                                    SendMessageBatchResultEntry.builder().id(entry.id()).build())
                            .toList())
                    .build());
    Mockito.doNothing().when(sqsDocumentServiceV2).delete(eq(partitionKey), anyList());

    // When
    underTest.sendAllRetryMessage(partitionKey, settings);

    // THEN
    verify(sqsDocumentServiceV2, times(0)).addIdRetryAttributeAndSaveRetry(eq(partitionKey), any());
    verify(sqsClient, times((int) Math.ceil(Double.valueOf(43) / 10D)))
        .sendMessageBatch(sendMessageBatchRequestCaptor.capture());
    verify(sqsDocumentServiceV2, times((int) Math.ceil(Double.valueOf(43) / 10D)))
        .delete(eq(partitionKey), deleteArgumentCaptor.capture());

    Assertions.assertThat(
            sendMessageBatchRequestCaptor.getAllValues().stream()
                .map(SendMessageBatchRequest::entries)
                .flatMap(Collection::stream)
                .toList()
                .size())
        .as("The number of message sent is correct")
        .isEqualTo(43);
    Assertions.assertThat(
            sendMessageBatchRequestCaptor.getAllValues().stream()
                .map(SendMessageBatchRequest::entries)
                .flatMap(Collection::stream)
                .toList())
        .as("The good message(s) body has been sent")
        .allMatch(
            sendMessageBatchRequestEntry ->
                sendMessageBatchRequestEntry
                    .messageBody()
                    .equals(
                        String.format(
                            "{ \"pmid\" : \"%s\" }",
                            sendMessageBatchRequestEntry
                                .id()
                                .substring(sendMessageBatchRequestEntry.id().length() - 2))));
    Assertions.assertThat(
            deleteArgumentCaptor.getAllValues().stream().flatMap(Collection::stream).toList())
        .as("The message has been deleted")
        .containsExactlyInAnyOrderElementsOf(documents.stream().map(SqsDocumentV2::getId).toList());
  }

  @ParameterizedTest
  @MethodSource("getArguments")
  public void should_save_and_send_n_messages(int endExclusive) {
    // GIVEN
    PartitionKey partitionKey =
        PartitionKey.builder().queueName("test-dlq").domain("wcc").env("test").build();
    SqsDlqSettingsDto settings = SqsDlqSettingsDto.builder().deleteWhenReplay(false).build();

    List<RetryDto> retries =
        IntStream.range(0, endExclusive)
            .mapToObj(Integer::valueOf)
            .map(
                i ->
                    RetryDto.builder()
                        .parentId(
                            String.format(
                                "1649059165761_3c5242e3-dacc-8495-ec5d-613411ec40%02d", i))
                        .retry(
                            SqsDocumentRetry.builder()
                                .attributes(Collections.emptyList())
                                .body(String.format("{ \"pmid\" : \"%02d\" }", i))
                                .sentTimestamp("1649062682756")
                                .status("Sent")
                                .build())
                        .build())
            .toList();

    when(sqsClient.sendMessageBatch(any(SendMessageBatchRequest.class)))
        .thenAnswer(
            invocation ->
                SendMessageBatchResponse.builder()
                    .successful(
                        invocation.getArgument(0, SendMessageBatchRequest.class).entries().stream()
                            .map(
                                entry ->
                                    SendMessageBatchResultEntry.builder().id(entry.id()).build())
                            .toList())
                    .build());
    when(sqsDocumentServiceV2.addIdRetryAttributeAndSaveRetry(eq(partitionKey), any()))
        .thenAnswer(
            invocation -> {
              return invocation.getArgument(1, RetryDto.class).getRetry();
            });

    // WHEN
    underTest.sendMultipleRetryMessage(partitionKey, retries, settings);
    // THEN

    // Assert that services are called
    verify(sqsDocumentServiceV2, times(endExclusive))
        .addIdRetryAttributeAndSaveRetry(
            eq(partitionKey), addIdRetryAttributeAndSaveRetryCaptor.capture());
    verify(sqsClient, times((int) Math.ceil(Double.valueOf(endExclusive) / 10D)))
        .sendMessageBatch(sendMessageBatchRequestCaptor.capture());
    verify(sqsDocumentServiceV2, times(0)).delete(eq(partitionKey), deleteArgumentCaptor.capture());

    Assertions.assertThat(addIdRetryAttributeAndSaveRetryCaptor.getAllValues())
        .as("id Has been added")
        .containsExactlyInAnyOrderElementsOf(retries);
    Assertions.assertThat(
            sendMessageBatchRequestCaptor.getAllValues().stream()
                .map(SendMessageBatchRequest::entries)
                .flatMap(Collection::stream)
                .toList()
                .size())
        .as("The number of message sent is correct")
        .isEqualTo(endExclusive);
    Assertions.assertThat(
            sendMessageBatchRequestCaptor.getAllValues().stream()
                .map(SendMessageBatchRequest::entries)
                .flatMap(Collection::stream)
                .toList())
        .as("The good message(s) body has been sent")
        .allMatch(
            sendMessageBatchRequestEntry ->
                sendMessageBatchRequestEntry
                    .messageBody()
                    .equals(
                        String.format(
                            "{ \"pmid\" : \"%s\" }",
                            sendMessageBatchRequestEntry
                                .id()
                                .substring(sendMessageBatchRequestEntry.id().length() - 2))));
  }

  @Test
  public void should_save_and_send_replay_all_messages() {
    // GIVEN
    PartitionKey partitionKey =
        PartitionKey.builder().queueName("test-dlq").domain("wcc").env("test").build();
    SqsDlqSettingsDto settings = SqsDlqSettingsDto.builder().deleteWhenReplay(false).build();
    List<SqsDocumentV2> documents =
        IntStream.range(0, 43)
            .mapToObj(Integer::valueOf)
            .map(
                i ->
                    SqsDocumentV2.builder()
                        .id(
                            String.format(
                                "1649059165761_3c5242e3-dacc-8495-ec5d-613411ec40%02d", i))
                        .body(String.format("{ \"pmid\" : \"%02d\" }", i))
                        .build())
            .toList();

    mockRepostitory(partitionKey, documents, 10);

    when(sqsClient.sendMessageBatch(any(SendMessageBatchRequest.class)))
        .thenAnswer(
            invocation ->
                SendMessageBatchResponse.builder()
                    .successful(
                        invocation.getArgument(0, SendMessageBatchRequest.class).entries().stream()
                            .map(
                                entry ->
                                    SendMessageBatchResultEntry.builder().id(entry.id()).build())
                            .toList())
                    .build());

    when(sqsDocumentServiceV2.addIdRetryAttributeAndSaveRetry(eq(partitionKey), any()))
        .thenAnswer(
            invocation -> {
              return invocation.getArgument(1, RetryDto.class).getRetry();
            });
    // When
    underTest.sendAllRetryMessage(partitionKey, settings);

    // THEN
    verify(sqsDocumentServiceV2, times(43))
        .addIdRetryAttributeAndSaveRetry(
            eq(partitionKey), addIdRetryAttributeAndSaveRetryCaptor.capture());
    verify(sqsClient, times((int) Math.ceil(Double.valueOf(43) / 10D)))
        .sendMessageBatch(sendMessageBatchRequestCaptor.capture());
    verify(sqsDocumentServiceV2, times(0)).delete(eq(partitionKey), deleteArgumentCaptor.capture());

    Assertions.assertThat(addIdRetryAttributeAndSaveRetryCaptor.getAllValues().size())
        .as("id Has been added")
        .isEqualTo(43);
    Assertions.assertThat(
            sendMessageBatchRequestCaptor.getAllValues().stream()
                .map(SendMessageBatchRequest::entries)
                .flatMap(Collection::stream)
                .toList()
                .size())
        .as("The number of message sent is correct")
        .isEqualTo(43);
    Assertions.assertThat(
            sendMessageBatchRequestCaptor.getAllValues().stream()
                .map(SendMessageBatchRequest::entries)
                .flatMap(Collection::stream)
                .toList())
        .as("The good message(s) body has been sent")
        .allMatch(
            sendMessageBatchRequestEntry ->
                sendMessageBatchRequestEntry
                    .messageBody()
                    .equals(
                        String.format(
                            "{ \"pmid\" : \"%s\" }",
                            sendMessageBatchRequestEntry
                                .id()
                                .substring(sendMessageBatchRequestEntry.id().length() - 2))));
  }

  private void mockRepostitory(
      PartitionKey partitionKey, List<SqsDocumentV2> documents, int pagination) {
    // Assuming that we are paging by TestPagination
    when(sqsDocumentRepositoryV2.fetchFirstPage(partitionKey, Optional.empty()))
        .thenReturn(documents.subList(0, pagination));

    when(sqsDocumentRepositoryV2.fetchNextPage(eq(partitionKey), anyString(), eq(Optional.empty())))
        .thenAnswer(
            invocation1 -> {
              String id = invocation1.getArgument(1, String.class);
              Integer endIndex = Integer.valueOf(id.substring(id.length() - 2));
              return documents.subList(endIndex + 1, Math.min(endIndex + 1 + pagination, 43));
            });
  }
}
