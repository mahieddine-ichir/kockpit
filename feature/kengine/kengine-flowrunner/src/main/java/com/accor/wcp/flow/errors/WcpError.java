package com.accor.wcp.flow.errors;

import java.util.Collections;
import java.util.Map;

public enum WcpError implements ErrorCode {
  TECHNICAL_ERROR(
      "Internal Server error", 500, "Unable to process data. Please contact Kockpit Platform Engineering - team.");

  private final String title;
  private final int status;
  private final String detail;
  private final Map<String, Object> parameters;

  WcpError(String title, int status, String detail, Map<String, Object> parameters) {
    this.title = title;
    this.status = status;
    this.detail = detail;
    this.parameters = parameters;
  }

  WcpError(String title, int status, String detail) {
    this(title, status, detail, Collections.emptyMap());
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public String getDetail() {
    return detail;
  }

  @Override
  public Map<String, Object> getParameters() {
    return parameters;
  }
}
