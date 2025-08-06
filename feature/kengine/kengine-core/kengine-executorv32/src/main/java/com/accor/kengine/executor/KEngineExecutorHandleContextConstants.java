package com.accor.kengine.executor;

/**
 * Constants context keys class for KEngine Flow service method handler. It defines 'built-in'
 * constants you can inject into your composite result class.
 */
public final class KEngineExecutorHandleContextConstants {

  /** To inject the {@link com.accor.kengine.execution.ExecutionResult}. */
  public static final String CONTEXT_FLOW_EXECUTION_RESULT_KEY = "flowExecutionResult";

  /** No usage for this constructor. */
  private KEngineExecutorHandleContextConstants() {
    // Constant class
  }
}
