package org.kockpit.rules.executor;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.rules.RuleExecutionException;
import org.kockpit.rules.RuleNode;
import org.kockpit.rules.WarningExecutionException;
import org.kockpit.rules.execution.ExecutionResult;
import org.kockpit.rules.registry.RuleNodeRegistry;
import org.kockpit.rules.registry.model.Rule;

import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class KEngineFlowRunnerImpl<T> {

  private final RuleNodeRegistry<T> registry;

  private final KEngineRuleNodeExecutorFactory KEngineRuleNodeExecutorFactory;

  private final Optional<KEngineFlowRunnerExecutionHandler> kEngineFlowRunnerExecutionHandler;

  public ExecutionResult execute(
      String flowId, Object context, String flowExecutionName, boolean audit) {
    ExecutionResult executionResult =
        executeFlowAndGetResult(
            flowId,
            context,
            KEngineRuleNodeExecutorFactory.createRuleNodeExecutor(flowExecutionName, audit));
    logExecutionId(executionResult.getExecutionId());
    if (!executionResult.isSuccessful()) {
      logAndThrowException(executionResult);
    }
    if (executionResult.isWarning()) {
      logWarnings(executionResult.getWarnings(), executionResult);
    }
    return executionResult;
  }

  private void logExecutionId(String executionId) {
    // Nothing to do here
    if (kEngineFlowRunnerExecutionHandler.isEmpty()) {
      return;
    }
    kEngineFlowRunnerExecutionHandler.get().logExecutionId(executionId);
  }

  private ExecutionResult executeFlowAndGetResult(
      String flowId, Object context, RuleNodeExecutor<T> ruleNodeExecutor) {
    try {
      return executeFlow(flowId, context, ruleNodeExecutor);
    } catch (RuleExecutionException e) {
      return e.getExecutionResult();
    }
  }

  private ExecutionResult executeFlow(
      String flowId, Object context, RuleNodeExecutor<T> ruleNodeExecutor) {
      return ruleNodeExecutor.execute(getRulesToExecute(flowId), (T) context);
  }

  private List<RuleNode<T>> getRulesToExecute(String flowId) {
    List<Rule<T>> flowRules = getFlowRules(flowId);
    return buildRuleNodesToExecute(flowRules);
  }

  private <T> List<RuleNode<T>> buildRuleNodesToExecute(List<Rule<T>> flowRules) {
    return flowRules.stream().map(Rule::getRuleNode).toList();
  }

  private List<Rule<T>> getFlowRules(String flowId) {
    return registry.getRulesByFlowId(flowId);
  }

  protected void logAndThrowException(ExecutionResult executionResult) {
    if (kEngineFlowRunnerExecutionHandler.isPresent()) {
      try {
        kEngineFlowRunnerExecutionHandler.get().logAndThrowException(executionResult);
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      return;
    }

    // Very simple implementation
    Throwable throwable = executionResult.getThrowable();
    log.error(throwable.getMessage(), throwable);
    throw new RuntimeException(throwable);
  }

  protected void logWarnings(List<WarningExecutionException> warnings, ExecutionResult executionResult) {
    // Nothing to do here
    if (kEngineFlowRunnerExecutionHandler.isEmpty()) {
      return;
    }
    kEngineFlowRunnerExecutionHandler.get().logWarnings(warnings, executionResult);
  }
}
