package com.accor.wcp.console.services.sqsdlq.dynamo.domain;

public enum SqsDocumentStatus {

  NEW ("NEW"),
  ANALYSIS_ONGOING ("ANALYSIS_ONGOING"),
  RESOLVED ("RESOLVED");

  private String value;

  SqsDocumentStatus(String value) {
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

