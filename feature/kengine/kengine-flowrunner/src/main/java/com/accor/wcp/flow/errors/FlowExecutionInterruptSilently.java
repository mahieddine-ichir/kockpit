package com.accor.wcp.flow.errors;

import com.accor.kengine.ExecutionInterruptionException;

public class FlowExecutionInterruptSilently extends ExecutionInterruptionException {

  public FlowExecutionInterruptSilently() {
    super(null);
  }
}
