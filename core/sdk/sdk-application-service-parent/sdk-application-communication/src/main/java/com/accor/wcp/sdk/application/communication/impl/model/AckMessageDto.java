package com.accor.wcp.sdk.application.communication.impl.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AckMessageDto implements Serializable {
  @Builder.Default private String internalType = "ACK";
  private String domain;
  private String env;
  private String applicationId;
  private String instanceId;
  private String messageId;
}
