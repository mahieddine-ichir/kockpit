package com.accor.wcp.flow;

import com.accor.kengine.DocumentationDetails;
import com.accor.kengine.KEngineRuleNodeExecutorFactory;
import com.accor.kengine.RuleExecutionException;
import com.accor.kengine.RuleNode;
import com.accor.kengine.RuleNodeExecutor;
import com.accor.kengine.execution.ExecutionResult;
import com.accor.kengine.registry.RuleNodeRegistry;
import com.accor.kengine.registry.model.Rule;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
class FlowRunnerImpl implements FlowRunner {

  private final RuleNodeRegistry registry;

  private final KEngineRuleNodeExecutorFactory ruleNodeExecutorFactory;
  private final DefaultKEngineFlowRunnerExecutionHandler flowRunnerExecutionHandler;

  FlowRunnerImpl(
      RuleNodeRegistry<?> registry,
      RuleNodeExecutorFactory ruleNodeExecutorFactory,
      DefaultKEngineFlowRunnerExecutionHandler flowRunnerExecutionHandler) {
    this.registry = registry;
    this.ruleNodeExecutorFactory = ruleNodeExecutorFactory;
    this.flowRunnerExecutionHandler = flowRunnerExecutionHandler;
  }

  @Override
  public ExecutionResult execute(
      DocumentationDetails flowDetails,
      FlowContextContainer context,
      String flowExecutionName,
      boolean audit) {
    ExecutionResult executionResult =
        executeFlowAndGetResult(
            flowDetails,
            context,
            ruleNodeExecutorFactory.createRuleNodeExecutor(flowExecutionName, audit));
    flowRunnerExecutionHandler.logExecutionId(executionResult.getExecutionId());
    if (!executionResult.isSuccessful()) {
      flowRunnerExecutionHandler.logAndThrowException(executionResult);
    }
    if (executionResult.isWarning()) {
      flowRunnerExecutionHandler.logWarnings(null, executionResult.getWarnings(), executionResult);
    }
    return executionResult;
  }

  public ExecutionResult execute(DocumentationDetails flowDetails, FlowContextContainer context) {
    return execute(flowDetails, context, null, true);
  }

  @Override
  public ExecutionResult execute(
      DocumentationDetails flowId, FlowContextContainer context, boolean audited) {
    return execute(flowId, context, null, audited);
  }

  @Override
  public ExecutionResult execute(
      DocumentationDetails flowId, FlowContextContainer context, String flowExecutionName) {
    return execute(flowId, context, flowExecutionName, true);
  }

  public ExecutionResult executeAndGetResult(
      DocumentationDetails flowDetails, FlowContextContainer context) {
    return executeAndGetResult(flowDetails, context, null);
  }

  @Override
  public ExecutionResult executeAndGetResult(
      DocumentationDetails flowDetails, FlowContextContainer context, String flowExecutionName) {
    return executeFlowAndGetResult(
        flowDetails, context, ruleNodeExecutorFactory.createRuleNodeExecutor(flowExecutionName));
  }

  @Override
  public ExecutionResult executeAndGetResult(
      DocumentationDetails flowDetails, FlowContextContainer context, boolean audited) {
    return executeFlowAndGetResult(
        flowDetails, context, ruleNodeExecutorFactory.createRuleNodeExecutor(audited));
  }

  @Override
  public ExecutionResult executeAndGetResult(
      DocumentationDetails flowDetails,
      FlowContextContainer context,
      String flowExecutionName,
      boolean audited) {
    return executeFlowAndGetResult(
        flowDetails,
        context,
        ruleNodeExecutorFactory.createRuleNodeExecutor(flowExecutionName, audited));
  }

  private ExecutionResult executeFlowAndGetResult(
      DocumentationDetails flowDetails,
      FlowContextContainer context,
      RuleNodeExecutor<FlowContextContainer> ruleNodeExecutor) {
    ExecutionResult executionResult;
    try {
      executionResult = executeFlow(flowDetails, context, ruleNodeExecutor);
    } catch (RuleExecutionException e) {
      executionResult = e.getExecutionResult();
    }
    return executionResult;
  }

  private ExecutionResult executeFlow(
      DocumentationDetails flowDetails,
      FlowContextContainer context,
      RuleNodeExecutor<FlowContextContainer> ruleNodeExecutor) {
    return ruleNodeExecutor.execute(getRulesToExecute(flowDetails), context);
  }

  private List<RuleNode<FlowContextContainer>> getRulesToExecute(DocumentationDetails flowDetails) {
    List<Rule<FlowContextContainer>> flowRules = getFlowRules(flowDetails);
    return buildRuleNodesToExecute(flowRules);
  }

  private List<RuleNode<FlowContextContainer>> buildRuleNodesToExecute(
      List<Rule<FlowContextContainer>> flowRules) {
    return flowRules.stream().map(Rule::getRuleNode).collect(Collectors.toList());
  }

  private List<Rule<FlowContextContainer>> getFlowRules(DocumentationDetails flowDetails) {
    return registry.getRulesByFlowId(flowDetails.getCode());
  }

  public static Logger getLog() {
    return log;
  }
}
