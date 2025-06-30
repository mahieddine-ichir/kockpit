package org.kockpit.rules;

import org.kockpit.rules.execution.RuleNodeExecution;
import lombok.Getter;

@Getter
public class RuleNodeException extends Throwable {

  private final RuleNodeExecution ruleNodeExecution;

  public RuleNodeException(RuleNodeExecution ruleNodeExecution, Throwable t) {
    super(t);
    this.ruleNodeExecution = ruleNodeExecution;
  }

}
