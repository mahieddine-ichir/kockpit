package com.accor.wcp.sdk.application.communication.impl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeartBitDto {
  private String domain;
  private String env;
  private String applicationId;
  private String applicationInstance;
  private String applicationVersion;
  @Builder.Default private long timestamp = System.currentTimeMillis();
  private boolean ended;
}
