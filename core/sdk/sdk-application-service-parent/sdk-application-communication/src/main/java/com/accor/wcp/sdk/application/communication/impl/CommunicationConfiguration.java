package com.accor.wcp.sdk.application.communication.impl;

import com.accor.wcp.sdk.application.SdkApplicationProperties;
import com.accor.wcp.sdk.application.SdkEnvironmentConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

import static java.util.Objects.nonNull;

/**
 * SDK "auto" communication configuration properties generator for: - mock - local - dev - bch - oat
 * - pro
 */
@Slf4j
@Configuration
class CommunicationConfiguration {

  @Value("${wcp.sdk.aws.kinesis.endpoint:http://localhost:4566}")
  private String kinesisEndpoint;

  @Value("${wcp.sdk.aws.dynamodb.endpoint:http://localhost:4566}")
  private String dynamoDbEndpoint;

  @Value("${wcp.sdk.aws.cloudwatch.endpoint:http://localhost:4566}")
  private String cloudWatchEndpoint;

  @Value("${wcp.sdk.notification.polling.interval-ms:5000}")
  private int notificationPollingIntervalMs;

  @Value("${wcp.sdk.hearbit.interval-ms:60000}")
  private int heartbitIntervalMs;

  @Value("${aws.region:eu-west-1}")
  private String awsRegion;

  @Bean
  public CommunicationConfigurationProperties communicationConfigurationProperties(
          SdkApplicationProperties sdkApplicationProperties,
          SdkEnvironmentConfig sdkEnvironmentConfig) throws IOException {

    log.info("WCP SDK - Using SDK properties: {}", sdkApplicationProperties);
    String sdkEnv = sdkApplicationProperties.getWcpEnv();
    CommunicationConfigurationProperties communicationConfigurationProperties =
            switch (sdkEnv) {
              case "local", "mock", "none" -> buildLocal();
              default -> buildFromConfig(sdkEnv, sdkEnvironmentConfig);
            };

    log.info(
            "WCP SDK - Using CommunicationConfigurationProperties: {}",
            communicationConfigurationProperties);
    return communicationConfigurationProperties;
  }

  private CommunicationConfigurationProperties buildFromConfig(String sdkEnv, SdkEnvironmentConfig sdkEnvironmentConfig) {
    return CommunicationConfigurationProperties.builder()
            .awsRegion(nonNull(sdkEnvironmentConfig.getAwsRegion()) ? sdkEnvironmentConfig.getAwsRegion() : awsRegion)
            .heartbitStreamName(sdkEnvironmentConfig.getHeartbeatStream())
            .app2wcpStreamName(sdkEnvironmentConfig.getApp2wcpStreamName())
//        .wcp2appStreamName("wcp-sdk-stream-communication-wcp2app-dev")
            .wcp2appBucket(sdkEnvironmentConfig.getWcp2appBucket())
            .kinesisEndpoint(null)
            .notificationPollingIntervalMs(nonNull(sdkEnvironmentConfig.getNotificationPollingIntervalMs()) ? sdkEnvironmentConfig.getNotificationPollingIntervalMs() : notificationPollingIntervalMs)
            .heartbitIntervalMs(nonNull(sdkEnvironmentConfig.getHeartbeatIntervalMs()) ? sdkEnvironmentConfig.getHeartbeatIntervalMs() : heartbitIntervalMs)
            .kinesisAssumeRoleName(sdkEnvironmentConfig.getAwsRoleName())
            .build();
  }

  private CommunicationConfigurationProperties buildLocal() {
    return CommunicationConfigurationProperties.builder()
        .awsRegion(awsRegion)
        .heartbitStreamName("wcp-sdk-stream-communication-heartbit-local")
        .app2wcpStreamName("wcp-sdk-stream-communication-app2wcp-local")
        .wcp2appStreamName("wcp-sdk-stream-communication-wcp2app-local")
        .wcp2appBucket("wcp-sdk-bucket-wcp2app-local")
        .kinesisEndpoint(kinesisEndpoint)
        .dynamoDbEndpoint(dynamoDbEndpoint)
        .cloudWatchEndpoint(cloudWatchEndpoint)
        .kinesisAssumeRoleName(null)
        .notificationPollingIntervalMs(notificationPollingIntervalMs)
        .heartbitIntervalMs(heartbitIntervalMs)
        .build();
  }
}
