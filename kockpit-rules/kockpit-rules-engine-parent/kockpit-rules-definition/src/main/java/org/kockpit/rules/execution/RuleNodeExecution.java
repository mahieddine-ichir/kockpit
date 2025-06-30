package org.kockpit.rules.execution;

import org.kockpit.rules.RuleNode;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class RuleNodeExecution {

  private final RuleNode<?> ruleNode;

  private List<StepExecution> stepExecutions = new LinkedList<>();

  private boolean ok;

  private RuleNodeExecution next;

  private Throwable throwable;

  private long timeInMs;

  private long startTimestamp;

  private long endTimestamp;

  private boolean warning;

  private boolean skipped;

  public RuleNodeExecution(RuleNode ruleNode) {
    this.ruleNode = ruleNode;
  }

    public RuleNodeExecution next() {
    return next;
  }

    public long getTimeInMs() {
    return endTimestamp - startTimestamp;
  }

    public RuleNodeExecution getTerminalNode() {
    RuleNodeExecution ruleNodeExecution = this;
    while (ruleNodeExecution.getNext() != null) {
      ruleNodeExecution = ruleNodeExecution.getNext();
    }
    return ruleNodeExecution;
  }

  @Override
  public String toString() {
    return "RuleNodeExecution{"
        + "stepExecutions="
        + stepExecutions
        + ", ok="
        + ok
        + ", next="
        + next
        + '}';
  }
}
