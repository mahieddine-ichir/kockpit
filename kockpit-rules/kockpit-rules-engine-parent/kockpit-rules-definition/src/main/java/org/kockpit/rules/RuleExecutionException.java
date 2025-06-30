package org.kockpit.rules;

import org.kockpit.rules.execution.ExecutionResult;
import org.kockpit.rules.execution.RuleExecution;
import lombok.Getter;

@Getter
public class RuleExecutionException extends RuntimeException {

  private final RuleExecution ruleExecution;
  private final ExecutionResult executionResult;

  public RuleExecutionException(
          RuleExecution ruleExecution, ExecutionResult executionResult, RuleNodeException e) {
    super(e);
    this.ruleExecution = ruleExecution;
    this.executionResult = executionResult;
  }

}
