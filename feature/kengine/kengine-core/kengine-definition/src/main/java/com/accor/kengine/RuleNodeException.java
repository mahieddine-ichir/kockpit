package com.accor.kengine;

import com.accor.kengine.execution.RuleNodeExecution;

public class RuleNodeException extends Throwable {

  private final RuleNodeExecution ruleNodeExecution;

  public RuleNodeException(RuleNodeExecution ruleNodeExecution, Throwable t) {
    super(t);
    this.ruleNodeExecution = ruleNodeExecution;
  }

  public RuleNodeExecution getRuleNodeExecution() {
    return ruleNodeExecution;
  }
}
