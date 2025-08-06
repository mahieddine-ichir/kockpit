package com.accor.wcp.sdk.application.communication.impl;

import static com.accor.wcp.sdk.application.communication.impl.AwsStsProviderUtils.roleCredentialsProvider;

import com.accor.wcp.aws.kinesis.producer.AbstractKinesisProducer;
import com.accor.wcp.sdk.application.SdkApplicationProperties;
import com.accor.wcp.sdk.application.communication.App2WCPConsoleCommunicationService;
import com.accor.wcp.sdk.application.communication.impl.model.AckMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordResponse;

/** App2Wcp communication through Kinesis producer implementation. */
@Slf4j
class App2WCPConsoleCommunicationServiceImpl extends AbstractKinesisProducer
    implements App2WCPConsoleCommunicationService {

  private final String streamName;
  private final ObjectMapper objectMapper;
  private final String applicationId;
  private final CommunicationConfigurationProperties communicationConfigurationProperties;
  private final ApplicationInstanceData applicationInstanceData;
  private final String domain;
  private final String env;

  App2WCPConsoleCommunicationServiceImpl(
      CommunicationConfigurationProperties communicationConfigurationProperties,
      SdkApplicationProperties sdkApplicationProperties,
      ApplicationInstanceData applicationInstanceData) {
    this.communicationConfigurationProperties = communicationConfigurationProperties;
    this.streamName = communicationConfigurationProperties.getApp2wcpStreamName();
    this.applicationId = sdkApplicationProperties.getApplicationId();
    this.applicationInstanceData = applicationInstanceData;
    this.domain = sdkApplicationProperties.getDomain();
    this.env = sdkApplicationProperties.getApplicationEnv();
    this.objectMapper = new ObjectMapper();
  }

  @Override
  protected String getKinesisEndpoint() {
    return communicationConfigurationProperties.getKinesisEndpoint();
  }

  @Override
  protected AwsCredentialsProvider getAwsCredentialsProvider() {
    String roleSessionName = "wcp-application-heartbit-session-" + Math.random();
    return roleCredentialsProvider(
        communicationConfigurationProperties.getKinesisAssumeRoleName(),
        roleSessionName,
        Region.of(communicationConfigurationProperties.getAwsRegion()));
  }

  String send(String streamName, ApplicationMessageNotificationDto message) {
    String json;
    try {
      json = objectMapper.writeValueAsString(message);
    } catch (JsonProcessingException e) {
      log.warn("Error serializing message: {}", message, e);
      return null;
    }

    PutRecordRequest putRecordRequest =
        PutRecordRequest.builder()
            .partitionKey(applicationId)
            .streamName(streamName)
            .data(SdkBytes.fromByteArray(json.getBytes(StandardCharsets.UTF_8)))
            .build();
    try {
      PutRecordResponse putRecordResponse = kinesisClient.putRecord(putRecordRequest).get();
      return putRecordResponse.sequenceNumber();
    } catch (ExecutionException e) {
      log.error(
          "Error sending record: {} to wcp console. Error: {}",
          putRecordRequest,
          e.getMessage(),
          e);
    } catch (InterruptedException e) {
      log.error(
          "Error sending record: {} to wcp console. Error: {}",
          putRecordRequest,
          e.getMessage(),
          e);
    }
    return null;
  }

  @Override
  public void ack(String serviceId, String messageId) {
    String instanceId = applicationInstanceData.getApplicationInstance();
    AckMessageDto ackMessageDto =
        AckMessageDto.builder()
            .domain(domain)
            .env(env)
            .applicationId(applicationId)
            .instanceId(instanceId)
            .messageId(messageId)
            .build();

    ApplicationMessageNotificationDto applicationMessageNotificationDto =
        ApplicationMessageNotificationDto.builder()
            .serviceId(serviceId)
            .domain(domain)
            .env(env)
            .applicationId(applicationId)
            .instanceId(instanceId)
            .requestMessageId(messageId)
            .message(ackMessageDto)
            .build();

    send(streamName, applicationMessageNotificationDto);
  }

  @Override
  public void notify(String serviceId, Serializable message) {
    String instanceId = applicationInstanceData.getApplicationInstance();
    ApplicationMessageNotificationDto applicationMessageNotificationDto =
        ApplicationMessageNotificationDto.builder()
            .serviceId(serviceId)
            .domain(domain)
            .env(env)
            .applicationId(applicationId)
            .instanceId(instanceId)
            .message(message)
            .build();

    send(streamName, applicationMessageNotificationDto);
  }

  @Override
  public void notifyResponse(String serviceId, String requestMessageId, Serializable message) {
    String instanceId = applicationInstanceData.getApplicationInstance();
    ApplicationMessageNotificationDto applicationMessageNotificationDto =
        ApplicationMessageNotificationDto.builder()
            .serviceId(serviceId)
            .domain(domain)
            .env(env)
            .applicationId(applicationId)
            .instanceId(instanceId)
            .message(message)
            .requestMessageId(requestMessageId)
            .build();

    send(streamName, applicationMessageNotificationDto);
  }
}
