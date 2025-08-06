package com.accor.wcp.console.services.sqsdlq;

import static java.util.Collections.emptyMap;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.accor.wcp.console.services.sqsdlq.dynamo.SqsDocumentRepositoryV2;
import com.accor.wcp.console.services.sqsdlq.dynamo.SqsDocumentServiceV2;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsAttributeType;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentRetry;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2;
import com.accor.wcp.console.services.sqsdlq.model.MessageDeduplication;
import com.accor.wcp.console.services.sqsdlq.model.PartitionKey;
import com.accor.wcp.console.services.sqsdlq.model.RetryDto;
import com.accor.wcp.console.services.sqsdlq.model.SqsDlqSettingsDto;
import com.google.common.collect.Lists;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResultEntry;

@Service
@Slf4j
class SqsAwsServiceV2 implements MessageDeduplication {

  public static final int SQS_BATCH_LIMIT = 10;
  private final Clock clock = Clock.systemDefaultZone();
  private final SqsClient sqsClient;
  private final SqsDocumentRepositoryV2 sqsDocumentRepositoryV2;
  private final SqsDocumentServiceV2 sqsDocumentServiceV2;
  private final Integer producerCount;

  public SqsAwsServiceV2(
      SqsClient sqsClient,
      SqsDocumentRepositoryV2 sqsDocumentRepositoryV2,
      SqsDocumentServiceV2 sqsDocumentServiceV2,
      @Value("${sqsdlq.replayer.threads:5}") Integer producerCount) {
    this.sqsClient = sqsClient;
    this.sqsDocumentRepositoryV2 = sqsDocumentRepositoryV2;
    this.sqsDocumentServiceV2 = sqsDocumentServiceV2;
    this.producerCount = producerCount;
  }

  private Map<String, MessageAttributeValue> constructMessageRetryAttributes(
      SqsDocumentRetry sqsMessageRetry) {
    if (isEmpty(sqsMessageRetry.getAttributes())) {
      return emptyMap();
    }

    return sqsMessageRetry.getAttributes().stream()
        .map(
            a -> {
              MessageAttributeValue messageAttributeValue = null;
              String typeValue = a.getType().getValue();
              if (SqsAttributeType.BINARY.getValue().equals(typeValue)) {
                messageAttributeValue =
                    MessageAttributeValue.builder()
                        .dataType(typeValue)
                        .binaryValue(SdkBytes.fromUtf8String(a.getValue()))
                        .build();
              } else {
                messageAttributeValue =
                    MessageAttributeValue.builder()
                        .stringValue(a.getValue())
                        .dataType(typeValue)
                        .build();
              }
              return Map.entry(a.getName(), messageAttributeValue);
            })
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public List<SqsDocumentRetry> sendMultipleRetryMessage(
      PartitionKey partitionKey, List<RetryDto> retries, SqsDlqSettingsDto settings)
      throws NotFoundException {
    Boolean deleteWhenReplay = settings.getDeleteWhenReplay();
    List<List<RetryDto>> partitions = Lists.partition(retries, SQS_BATCH_LIMIT);
    ForkJoinPool customThreadPool = new ForkJoinPool(Math.min(producerCount, partitions.size()));
    List<List<RetryDto>> join =
        customThreadPool
            .submit(
                () ->
                    partitions.parallelStream()
                        .map(
                            partition ->
                                retryOnePartition(
                                    partitionKey, settings, deleteWhenReplay, partition))
                        .toList())
            .join();
    return join.stream().flatMap(Collection::stream).map(RetryDto::getRetry).toList();
  }

  private List<RetryDto> retryOnePartition(
      PartitionKey partitionKey,
      SqsDlqSettingsDto settings,
      Boolean deleteWhenReplay,
      List<RetryDto> partition) {
    List<RetryDto> retriesWithId =
        deleteWhenReplay
            ? partition
            : partition.stream()
                .map(
                    r ->
                        RetryDto.builder()
                            .parentId(r.getParentId())
                            .retry(
                                sqsDocumentServiceV2.addIdRetryAttributeAndSaveRetry(
                                    partitionKey, r))
                            .build())
                .toList();

    List<String> successSqsIds = sqsSendMessageBatch(settings, retriesWithId);
    if (deleteWhenReplay && !isEmpty(successSqsIds)) {
      sqsDocumentServiceV2.delete(partitionKey, successSqsIds);
    }
    return retriesWithId.stream().filter(r -> successSqsIds.contains(r.getParentId())).toList();
  }

  private List<String> sqsSendMessageBatch(
      SqsDlqSettingsDto settings, List<RetryDto> sqsDocumentRetries) {
    List<SendMessageBatchRequestEntry> entries =
        sqsDocumentRetries.stream()
            .map(
                r ->
                    SendMessageBatchRequestEntry.builder()
                        .id(r.getParentId())
                        .messageBody(r.getRetry().getBody())
                        .messageAttributes(constructMessageRetryAttributes(r.getRetry()))
                        .messageGroupId(r.getRetry().getGroupId())
                        .messageDeduplicationId(constructMessageDeduplicationId(r.getRetry().getBody(), settings.getDedupIdGenerationStrategy()))
                        .build())
            .toList();
    SendMessageBatchRequest sendMessageBatchRequest =
        SendMessageBatchRequest.builder().queueUrl(settings.getUrl()).entries(entries).build();
    // Return successIds
    return sqsClient.sendMessageBatch(sendMessageBatchRequest).successful().stream()
        .map(SendMessageBatchResultEntry::id)
        .toList();
  }

  public void sendAllRetryMessage(PartitionKey partitionKey, SqsDlqSettingsDto settings)
      throws NotFoundException {
    log.debug("Send ALL messages for: '{}'", partitionKey.toString());
    long startTimestamp = Instant.now(clock).toEpochMilli();
    List<SqsDocumentV2> entities =
        sqsDocumentRepositoryV2.fetchFirstPage(partitionKey, Optional.empty());
    Optional<String> sortKey = retryChunk(partitionKey, entities, settings);
    while (sortKey.isPresent()) {
      entities =
          sqsDocumentRepositoryV2.fetchNextPage(partitionKey, sortKey.get(), Optional.empty());
      checkInfiniteLoopForBuggedApplication(partitionKey, entities, startTimestamp);
      sortKey = retryChunk(partitionKey, entities, settings);
    }
  }

  private void checkInfiniteLoopForBuggedApplication(
      PartitionKey partitionKey, List<SqsDocumentV2> entities, long startTimestamp) {
    for (var entity : entities) {
      long sentTimestamp = Long.parseLong(entity.getId().split(PartitionKey.KEY_SEPARATOR)[0]);
      if (sentTimestamp > startTimestamp) {
        throw new InfiniteLoopRiskException(partitionKey);
      }
    }
  }

  private Optional<String> retryChunk(
      PartitionKey partitionKey, List<SqsDocumentV2> entities, SqsDlqSettingsDto settings) {
    List<RetryDto> retryDtos = entities.stream().map(this::createRetry).toList();
    int size = retryDtos.size();
    if (size == 0) {
      return Optional.empty();
    }
    sendMultipleRetryMessage(partitionKey, retryDtos, settings);
    return Optional.of(retryDtos.get(size - 1).getParentId());
  }

  private RetryDto createRetry(SqsDocumentV2 e) {
    return RetryDto.builder()
        .parentId(e.getId())
        .retry(
            SqsDocumentRetry.builder()
                .groupId(e.getGroupId())
                .body(e.getBody())
                .attributes(e.getAttributes())
                .sentTimestamp(String.valueOf(new Date().getTime()))
                .receiveTime(null)
                .status("Sent")
                .build())
        .build();
  }
}
