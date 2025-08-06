package com.accor.wcp.flow.errors;

public class TechnicalError extends FlowExecutionError {

  public TechnicalError(WcpError errorCode) {
    super(errorCode);
  }
}
