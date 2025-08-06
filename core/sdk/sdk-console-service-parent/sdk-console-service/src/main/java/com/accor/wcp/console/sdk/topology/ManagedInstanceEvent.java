package com.accor.wcp.console.sdk.topology;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ManagedInstanceEvent {

  private String domain;
  private String env;
  private String applicationId;
  private String applicationInstance;
  private String applicationVersion;

  private ManagedInstanceStatus status;
}
