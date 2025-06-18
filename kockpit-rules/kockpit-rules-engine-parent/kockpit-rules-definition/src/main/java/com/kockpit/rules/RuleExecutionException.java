package com.kockpit.rules;

import com.kockpit.rules.execution.ExecutionResult;
import com.kockpit.rules.execution.RuleExecution;

public class RuleExecutionException extends RuntimeException {

  private final RuleExecution ruleExecution;
  private final ExecutionResult executionResult;

  public RuleExecutionException(
          RuleExecution ruleExecution, ExecutionResult executionResult, RuleNodeException e) {
    super(e);
    this.ruleExecution = ruleExecution;
    this.executionResult = executionResult;
  }

  public RuleExecution getRuleExecution() {
    return ruleExecution;
  }

  public ExecutionResult getExecutionResult() {
    return executionResult;
  }
}
