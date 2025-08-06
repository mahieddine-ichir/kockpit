package com.accor.wcp.sdk.application.communication.impl.model;

import com.accor.wcp.sdk.application.service.ServiceMessageNotification;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ServiceMessageFromWCPConsoleDto implements ServiceMessageNotification {
  private String internalType;
  private String messageId;
  private String domain;
  private String env;
  private String applicationId;
  private String instanceId;
  private String serviceId;
  private Map<String, Object> message;
}
