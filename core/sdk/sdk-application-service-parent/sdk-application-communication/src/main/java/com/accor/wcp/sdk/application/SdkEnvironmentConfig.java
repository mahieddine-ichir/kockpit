package com.accor.wcp.sdk.application;

import lombok.Data;

@Data
public class SdkEnvironmentConfig {

  private String awsAccountId;
  private String awsRegion;
  private String awsRoleName;
  private String heartbeatStream;
  private String app2wcpStreamName;
  private String wcp2appBucket;
  private Integer notificationPollingIntervalMs;
  private Integer heartbeatIntervalMs;

}
