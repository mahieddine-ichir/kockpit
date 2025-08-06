package com.accor.wcp.flow.errors;

import com.accor.kengine.WarningExecutionException;
import java.util.List;
import java.util.stream.Collectors;

public class FlowExecutionMultiWarning extends WarningExecutionException {

  private String executionId;
  private String log;
  private final List<ErrorCode> errorCodes;

  public FlowExecutionMultiWarning(List<ErrorCode> errorCodes) {
    super("MULTI_WARNING");
    this.errorCodes =
        errorCodes.stream()
            .map(
                errorCode ->
                    new ErrorCodeImpl(
                        errorCode.getTitle(),
                        errorCode.getStatus(),
                        errorCode.getDetail(),
                        errorCode.getParameters(),
                        errorCode.name()))
            .collect(Collectors.toList());
  }

  public String getExecutionId() {
    return executionId;
  }

  public FlowExecutionMultiWarning withExecutionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public List<ErrorCode> getErrorCodes() {
    return errorCodes;
  }

  public String getLog() {
    return log;
  }

  public FlowExecutionMultiWarning withLog(String log) {
    this.log = log;
    return this;
  }

  public FlowExecutionMultiWarning withLog(String log, Object... args) {
    this.log = String.format(log, args);
    return this;
  }
}
