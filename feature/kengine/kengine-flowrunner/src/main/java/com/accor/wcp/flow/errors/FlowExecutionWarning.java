package com.accor.wcp.flow.errors;

import com.accor.kengine.WarningExecutionException;

public class FlowExecutionWarning extends WarningExecutionException {

  private String executionId;
  private String log;
  private final ErrorCode errorCode;

  public FlowExecutionWarning(ErrorCode errorCode) {
    super(errorCode.getTitle());
    this.errorCode =
        new ErrorCodeImpl(
            errorCode.getTitle(),
            errorCode.getStatus(),
            errorCode.getDetail(),
            errorCode.getParameters(),
            errorCode.name());
  }

  public String getExecutionId() {
    return executionId;
  }

  public FlowExecutionWarning withExecutionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }

  public String getLog() {
    return log;
  }

  public FlowExecutionWarning withLog(String log) {
    this.log = log;
    return this;
  }

  public FlowExecutionWarning withLog(String log, Object... args) {
    this.log = String.format(log, args);
    return this;
  }
}
