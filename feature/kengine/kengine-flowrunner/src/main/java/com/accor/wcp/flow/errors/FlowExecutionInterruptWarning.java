package com.accor.wcp.flow.errors;

import com.accor.kengine.WarningExecutionException;

public class FlowExecutionInterruptWarning extends WarningExecutionException {

  private String executionId;
  private String log;
  private final ErrorCode errorCode;

  public FlowExecutionInterruptWarning(ErrorCode errorCode) {
    super(errorCode.getTitle(), true);
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

  public FlowExecutionInterruptWarning withExecutionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }

  public String getLog() {
    return log;
  }

  public FlowExecutionInterruptWarning withLog(String log) {
    this.log = log;
    return this;
  }

  public FlowExecutionInterruptWarning withLog(String log, Object... args) {
    this.log = String.format(log, args);
    return this;
  }
}
