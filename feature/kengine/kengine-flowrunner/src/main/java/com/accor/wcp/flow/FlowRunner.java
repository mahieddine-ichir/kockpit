package com.accor.wcp.flow;

import com.accor.kengine.DocumentationDetails;
import com.accor.kengine.execution.ExecutionResult;

/**
 * Flow runner hides steps to run KEngine Flow. You can execute a flow - by its id - by its id and
 * assign a flow execution name - disable audit of flow execution (by default audit is enabled)
 *
 * <p>Deprecating #executeAndGetResult in order to return execution result in execute method.
 */
public interface FlowRunner {

  /** Simple execution (no name, audit enabled). */
  ExecutionResult execute(DocumentationDetails flowId, FlowContextContainer context);

  /** Execution which audit could be disabled. */
  ExecutionResult execute(
      DocumentationDetails flowId, FlowContextContainer context, boolean audited);

  /** Execution with an execution name. */
  default ExecutionResult execute(
      DocumentationDetails flowId, FlowContextContainer context, String flowExecutionName) {
    return execute(flowId, context);
  }

  /** Execution with an execution name and audit could be disabled. */
  ExecutionResult execute(
      DocumentationDetails flowId,
      FlowContextContainer context,
      String flowExecutionName,
      boolean audited);

  @Deprecated
  ExecutionResult executeAndGetResult(
      DocumentationDetails flowDetails, FlowContextContainer context);

  @Deprecated
  default ExecutionResult executeAndGetResult(
      DocumentationDetails flowDetails, FlowContextContainer context, String flowExecutionName) {
    return executeAndGetResult(flowDetails, context);
  }

  default ExecutionResult executeAndGetResult(
      DocumentationDetails flowDetails, FlowContextContainer context, boolean audited) {
    return executeAndGetResult(flowDetails, context);
  }

  default ExecutionResult executeAndGetResult(
      DocumentationDetails flowDetails,
      FlowContextContainer context,
      String flowExecutionName,
      boolean audited) {
    return executeAndGetResult(flowDetails, context, flowExecutionName);
  }
}
