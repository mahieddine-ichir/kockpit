package com.accor.kengine.executor;

import com.accor.kengine.WarningExecutionException;
import com.accor.kengine.execution.ExecutionResult;
import java.util.List;

/**
 * Retro-compatibility interface definition to handle log / execution result in Runner. Should be
 * removed in v4.
 */
public interface KEngineFlowRunnerExecutionHandler {

  void logExecutionId(String executionId);

  void logAndThrowException(ExecutionResult executionResult) throws Exception;

  void logWarnings(List<WarningExecutionException> warnings, ExecutionResult executionResult);
}
