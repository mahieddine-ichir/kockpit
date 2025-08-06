package com.accor.wcp.sample.sampleflow.flow.rule;

import com.accor.kengine.DocumentationDetails;

public enum BusinessRule implements DocumentationDetails {
  BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_SIMPLE_RULE("A Simple rule!"),
  BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_SIMPLE_RULE2("A Simple rule bis!"),
  BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2("Another random rule!"),
  BR_SLOW_RULE("A slow (3sec execution) rule!"),
  BR_CALLER_RULE("A synchronized caller flow"),
  BR_OPERATION_RULE("Operation rule"),
  BR_ERROR_RULE("Error rule");

  private final String description;

  BusinessRule(String description) {
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
