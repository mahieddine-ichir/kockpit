package com.kockpit.rules.executor;

import com.kockpit.rules.RuleExecutionException;
import com.kockpit.rules.RuleNode;
import com.kockpit.rules.WarningExecutionException;
import com.kockpit.rules.execution.ExecutionResult;
import com.kockpit.rules.registry.RuleNodeRegistry;
import com.kockpit.rules.registry.model.Rule;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/** @deprecated Internal usage in KEngine only. This class should not have a public visibility */
@Slf4j
@Deprecated
public class KEngineFlowRunnerImpl {

  private final RuleNodeRegistry registry;

  private final KEngineRuleNodeExecutorFactory KEngineRuleNodeExecutorFactory;

  private final Optional<KEngineFlowRunnerExecutionHandler> kEngineFlowRunnerExecutionHandler;

  public KEngineFlowRunnerImpl(
      RuleNodeRegistry registry,
      KEngineRuleNodeExecutorFactory kEngineRuleNodeExecutorFactory,
      Optional<KEngineFlowRunnerExecutionHandler> kEngineFlowRunnerExecutionHandler) {
    this.registry = registry;
    this.KEngineRuleNodeExecutorFactory = kEngineRuleNodeExecutorFactory;
    this.kEngineFlowRunnerExecutionHandler = kEngineFlowRunnerExecutionHandler;
  }

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
      String flowId, Object context, RuleNodeExecutor ruleNodeExecutor) {
    ExecutionResult executionResult;
    try {
      executionResult = executeFlow(flowId, context, ruleNodeExecutor);
    } catch (RuleExecutionException e) {
      executionResult = e.getExecutionResult();
    }
    return executionResult;
  }

  private ExecutionResult executeFlow(
      String flowId, Object context, RuleNodeExecutor ruleNodeExecutor) {
    return ruleNodeExecutor.execute(getRulesToExecute(flowId), context);
  }

  private List<RuleNode> getRulesToExecute(String flowId) {
    List<Rule> flowRules = getFlowRules(flowId);
    return buildRuleNodesToExecute(flowRules);
  }

  private List<RuleNode> buildRuleNodesToExecute(List<Rule> flowRules) {
    return flowRules.stream().map(Rule::getRuleNode).toList();
  }

  private List<Rule> getFlowRules(String flowId) {
    return registry.getRulesByFlowId(flowId);
  }

  @Deprecated
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

  @Deprecated
  protected void logWarnings(List<WarningExecutionException> warnings, ExecutionResult executionResult) {
    // Nothing to do here
    if (kEngineFlowRunnerExecutionHandler.isEmpty()) {
      return;
    }
    kEngineFlowRunnerExecutionHandler.get().logWarnings(warnings, executionResult);
  }
}
