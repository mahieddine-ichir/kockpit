package com.accor.wcp.sdk.application.communication.impl;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class CommunicationConfigurationProperties {
  private String dynamoDbEndpoint;
  private String cloudWatchEndpoint;
  private String kinesisEndpoint;
  private String heartbitStreamName;
  private String wcp2appStreamName;
  private String wcp2appBucket;
  private String app2wcpStreamName;
  private String kinesisAssumeRoleName;
  private String awsRegion;
  private int notificationPollingIntervalMs;
  private int heartbitIntervalMs;
}
