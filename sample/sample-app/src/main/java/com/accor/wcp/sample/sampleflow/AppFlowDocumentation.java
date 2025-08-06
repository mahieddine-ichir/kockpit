package com.accor.wcp.sample.sampleflow;

import com.accor.kengine.DocumentationDetails;

public enum AppFlowDocumentation implements DocumentationDetails {
  FLOW_SIMPLE_FLOW("A simple flow"),
  FLOW_CALLER_FLOW("A caller flow in a flow"),
  FLOW_SUBFLOW_SLOW("A SLOW sub flow"),
  FLOW_OPERATION_FLOW("A operation flow"),

  FLOW_ERROR_FLOW("A flow in error");

  private final String description;

  AppFlowDocumentation(String description) {
    this.description = description;
  }

  @Override
  public String getCode() {
    return this.name();
  }

  @Override
  public String getDocumentation() {
    return description;
  }
}
