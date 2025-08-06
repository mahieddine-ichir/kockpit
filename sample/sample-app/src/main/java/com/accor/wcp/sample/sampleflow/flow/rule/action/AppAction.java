package com.accor.wcp.sample.sampleflow.flow.rule.action;

import com.accor.kengine.DocumentationDetails;

public enum AppAction implements DocumentationDetails {
  ACT_SIMPLE_ACTION("A simple action saying hello"),
  PRE_RANDOM_PREDICATE("A random false/true predicate"),
  ACT_SLOW_ACTION("Wait for 3sec"),
  ACT_CALL_ACTION("Call another flow from current flow execution"),
  ACT_CALL_NOAUDIT_ACTION("Call another flow from current flow execution with no audit"),
  ACT_SIMPLE_CACHE_ACTION("A simple action saying hello"),
  PRE_IN_CACHE_PREDICATE("a predicate"),
  PRE_OPERATION_IN_CACHE_PREDICATE("Check if the operation is already in cache"),
  ACT_LOAD_FROM_CACHE_ACTION("load result from cache"),
  PRE_IS_VALID_OPERATION_PREDICATE("Check if the operation is valid"),
  PRE_IS_VALID_VALUES_PREDICATE("Check if the values are valid"),
  ACT_DO_OPERATION_ACTION("Do a simple operation"),
  ACT_SET_ERROR_VALUES_ACTION("Wrong values error"),
  ACT_SET_ERROR_OPERATION_ACTION("Wrong operation symbol error"),
  ACT_ERROR_ACTION("action in error"),

  PRE_ERROR_PREDICATE("predicate in error");

  private final String description;

  AppAction(String description) {

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
