package com.accor.wcp.console.services.core.communication.wcp2app;

import static java.util.Objects.nonNull;

import com.accor.wcp.console.sdk.communication.WCPConsole2AppCommunicationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Slf4j
@Component
// @ConditionalOnProperty("wcp.sdk.communication.wcp2app.s3")
class ServiceMessageS3Producer implements WCPConsole2AppCommunicationService {

  private final String bucket;
  private final S3Client amazonS3;
  private final ObjectMapper objectMapper;

  ServiceMessageS3Producer(
      @Value("${wcp.sdk.communication.wcp2app.s3}") String bucket, S3Client amazonS3) {
    this.bucket = bucket;
    this.amazonS3 = amazonS3;
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public String broadcast(
      String serviceId, String domain, String env, String applicationId, Serializable message) {
    ServiceMessageDto serviceMessageDto =
        ServiceMessageDto.builder()
            .serviceId(serviceId)
            .applicationId(applicationId)
            .domain(domain)
            .env(env)
            .message(message)
            .build();
    return internalSend(
        serviceId, domain, env, applicationId, serviceMessageDto.getMessageId(), serviceMessageDto);
  }

  @Override
  public String send(
      String serviceId,
      String domain,
      String env,
      String applicationId,
      String instanceId,
      Serializable message) {
    ServiceInstanceMessageDto serviceMessageDto =
        ServiceInstanceMessageDto.builder()
            .serviceId(serviceId)
            .applicationId(applicationId)
            .domain(domain)
            .env(env)
            .instanceId(instanceId)
            .message(message)
            .build();
    return internalSend(
        serviceId, domain, env, applicationId, serviceMessageDto.getMessageId(), serviceMessageDto);
  }

  private String internalSend(
      String serviceId,
      String domain,
      String env,
      String applicationId,
      String messageId,
      Object serviceMessageDto) {
    String json;
    try {
      json = objectMapper.writeValueAsString(serviceMessageDto);
    } catch (JsonProcessingException e) {
      log.warn("Error serializing service message: {}", serviceMessageDto, e);
      return null;
    }

    // Create prefix
    String prefix = computePrefix(domain, env, applicationId);

    // Create key
    String keyName = prefix + "/" + messageId + ".json";

    // Put object
    try {
      PutObjectRequest putObjectRequest =
          PutObjectRequest.builder()
              .bucket(bucket)
              .key(keyName)
              .contentType(MediaType.APPLICATION_JSON.toString())
              .build();
      RequestBody requestBody = RequestBody.fromString(json);
      amazonS3.putObject(putObjectRequest, requestBody);

      return messageId;
    } catch (SdkClientException e) {
      log.error(
          "Error sending message: {} to s3 bucket service message. Error: {}",
          json,
          e.getMessage(),
          e);
    }
    return null;
  }

  private String computePrefix(String domain, String env, String applicationId) {
    String path = domain + "/" + env;
    if (nonNull(applicationId)) {
      path += "/" + applicationId;
    } else {
      path += "/all";
    }
    return path;
  }

  /**
   * No need to keep long time previous notifications as we use them to communicate between WCP and
   * Apps ("real time").
   */
  @Scheduled(
      fixedDelayString = "${wcp.sdk.communication.wcp2app.s3.clean.delay-seconds:60}",
      timeUnit = TimeUnit.SECONDS)
  void autoCleanOldNotifications() {
    List<ObjectIdentifier> idsToDelete = getExpiredNotificationIdentifiers();
    if (idsToDelete.isEmpty()) {
      return;
    }

    log.info("Deleting {} old wcp sdk notifications", idsToDelete.size());
    amazonS3.deleteObjects(
        DeleteObjectsRequest.builder()
            .bucket(bucket)
            .delete(Delete.builder().objects(idsToDelete).build())
            .build());
  }

  private List<ObjectIdentifier> getExpiredNotificationIdentifiers() {
    ListObjectsRequest listObjects = ListObjectsRequest.builder().bucket(bucket).build();

    ListObjectsResponse res = amazonS3.listObjects(listObjects);
    List<S3Object> objects = res.contents();

    // Remove previous notification from now - 60s
    Instant lastCheckpoint = Instant.now().minus(60, ChronoUnit.SECONDS);

    return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(objects.listIterator(), Spliterator.NONNULL), false)
        .filter(s3Object -> s3Object.lastModified().isBefore(lastCheckpoint))
        .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
        .toList();
  }
}
