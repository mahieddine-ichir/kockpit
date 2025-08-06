package com.accor.wcp.sdk.application.communication.impl;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApplicationMessageNotificationDto {

  String serviceId;
  String requestMessageId;
  String domain;
  String env;
  String applicationId;
  String instanceId;
  Serializable message;
}
