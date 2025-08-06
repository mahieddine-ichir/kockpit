package com.accor.wcp.console.services.sqsdlq.dynamo.domain;

public enum SqsAttributeType {
  NUMBER("Number"),
  STRING("String"),
  BINARY("Binary");

  private String value;

  SqsAttributeType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }
}
