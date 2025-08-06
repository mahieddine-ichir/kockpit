package com.accor.kengine.executor;

import com.accor.kengine.KEngineRuleNodeExecutorFactory;
import com.accor.kengine.RuleExecutionException;
import com.accor.kengine.RuleNode;
import com.accor.kengine.RuleNodeExecutor;
import com.accor.kengine.WarningExecutionException;
import com.accor.kengine.execution.ExecutionResult;
import com.accor.kengine.registry.RuleNodeRegistry;
import com.accor.kengine.registry.model.Rule;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

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
  protected void logWarnings(
      List<WarningExecutionException> warnings, ExecutionResult executionResult) {
    // Nothing to do here
    if (kEngineFlowRunnerExecutionHandler.isEmpty()) {
      return;
    }
    kEngineFlowRunnerExecutionHandler.get().logWarnings(warnings, executionResult);
  }
}
