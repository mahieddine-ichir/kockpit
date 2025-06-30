package org.kockpit.rules.executor;

import org.kockpit.rules.WarningExecutionException;
import org.kockpit.rules.execution.ExecutionResult;

import java.util.List;

public interface KEngineFlowRunnerExecutionHandler {

  void logExecutionId(String executionId);

  void logAndThrowException(ExecutionResult executionResult) throws Exception;

  void logWarnings(List<WarningExecutionException> warnings, ExecutionResult executionResult);
}
