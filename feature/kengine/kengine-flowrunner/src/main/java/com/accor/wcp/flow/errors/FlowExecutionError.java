package com.accor.wcp.flow.errors;

import com.accor.kengine.ErrorExecutionException;

public class FlowExecutionError extends ErrorExecutionException {

  private String executionId;
  private String log;
  private final ErrorCode errorCode;

  public FlowExecutionError(ErrorCode errorCode) {
    super(errorCode.getTitle());
    this.errorCode =
        new ErrorCodeImpl(
            errorCode.getTitle(),
            errorCode.getStatus(),
            errorCode.getDetail(),
            errorCode.getParameters(),
            errorCode.name());
  }

  public FlowExecutionError(ErrorCode errorCode, Throwable throwable) {
    super(errorCode.getTitle(), throwable);
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

  public FlowExecutionError withExecutionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }

  public String getLog() {
    return log;
  }

  public FlowExecutionError withLog(String log) {
    this.log = log;
    return this;
  }

  public FlowExecutionError withLog(String log, Object... args) {
    this.log = String.format(log, args);
    return this;
  }
}
