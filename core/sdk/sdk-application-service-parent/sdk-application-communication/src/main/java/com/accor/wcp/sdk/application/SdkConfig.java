package com.accor.wcp.sdk.application;

import lombok.Data;

import java.util.Map;

@Data
public class SdkConfig {

  private String name;
  private String description;
  private Map<String, SdkEnvironmentConfig> environments;
}
