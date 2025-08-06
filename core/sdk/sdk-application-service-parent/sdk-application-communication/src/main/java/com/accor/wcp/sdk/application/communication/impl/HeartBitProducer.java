package com.accor.wcp.sdk.application.communication.impl;

import static com.accor.wcp.sdk.application.communication.impl.AwsStsProviderUtils.roleCredentialsProvider;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.accor.wcp.sdk.application.SdkApplicationProperties;
import com.accor.wcp.sdk.application.communication.impl.model.HeartBitDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.kinesis.common.KinesisClientUtil;

/** SDK Heartbit(=heartbeat) producer through Kinesis implementation. */
@Component
@Slf4j
class HeartBitProducer {

  private final ObjectMapper objectMapper;
  private final ApplicationInstanceData applicationInstanceData;
  private final CommunicationConfigurationProperties communicationConfigurationProperties;
  private final SdkApplicationProperties sdkApplicationProperties;
  private KinesisAsyncClient kinesisClient;

  HeartBitProducer(
      CommunicationConfigurationProperties communicationConfigurationProperties,
      ApplicationInstanceData applicationInstanceData,
      SdkApplicationProperties sdkApplicationProperties) {
    this.communicationConfigurationProperties = communicationConfigurationProperties;
    this.applicationInstanceData = applicationInstanceData;
    this.sdkApplicationProperties = sdkApplicationProperties;
    this.objectMapper = new ObjectMapper();
  }

  @PostConstruct
  void init() {
    Region region = Region.EU_WEST_1;
    String kinesisEndpoint = communicationConfigurationProperties.getKinesisEndpoint();
    if (isNotEmpty(kinesisEndpoint)) {
      // Local dev
      kinesisClient =
          KinesisClientUtil.createKinesisAsyncClient(
              KinesisAsyncClient.builder()
                  .endpointOverride(URI.create(kinesisEndpoint))
                  .httpClientBuilder(
                      NettyNioAsyncHttpClient.builder()
                          .maxConcurrency(10)
                          .maxPendingConnectionAcquires(1000))
                  .region(region));
    } else {
      String roleSessionName = "wcp-application-heartbit-session-" + Math.random();
      AwsCredentialsProvider stsProvider =
          roleCredentialsProvider(
              communicationConfigurationProperties.getKinesisAssumeRoleName(),
              roleSessionName,
              region);

      kinesisClient =
          KinesisClientUtil.createKinesisAsyncClient(
              KinesisAsyncClient.builder()
                  .credentialsProvider(stsProvider)
                  .httpClientBuilder(
                      NettyNioAsyncHttpClient.builder()
                          .maxConcurrency(10)
                          .maxPendingConnectionAcquires(1000))
                  .region(region));
    }

    log.info(
        "WCP SDK - Heartbit configured with SDK configuration : {}",
        communicationConfigurationProperties);
  }

  void sendHeartBit(boolean ended) {
    HeartBitDto heartBitDto =
        HeartBitDto.builder()
            .domain(sdkApplicationProperties.getDomain())
            .env(sdkApplicationProperties.getApplicationEnv())
            .applicationId(applicationInstanceData.getApplicationId())
            .applicationInstance(applicationInstanceData.getApplicationInstance())
            .applicationVersion(sdkApplicationProperties.getApplicationVersion())
            .ended(ended)
            .build();
    String json;
    try {
      json = objectMapper.writeValueAsString(heartBitDto);
    } catch (JsonProcessingException e) {
      log.warn("Error serializing heart bit: {}", heartBitDto, e);
      return;
    }

    PutRecordRequest putRecordRequest =
        PutRecordRequest.builder()
            .partitionKey(this.applicationInstanceData.getApplicationId())
            .streamName(this.communicationConfigurationProperties.getHeartbitStreamName())
            .data(SdkBytes.fromByteArray(json.getBytes(StandardCharsets.UTF_8)))
            .build();
    try {
      kinesisClient.putRecord(putRecordRequest).get();
    } catch (ExecutionException | InterruptedException e) {
      log.error(
          "Error sending record: {} to kinesis heartbit. Error: {}",
          putRecordRequest,
          e.getMessage(),
          e);
    }
  }
}
