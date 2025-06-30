package org.kockpit.rules.execution;

import org.kockpit.rules.ExecutionInterruptionException;
import org.kockpit.rules.RuleNode;
import lombok.Getter;
import lombok.Setter;

@Getter
public class RuleExecution {

  private final RuleNode<?> ruleNode;

  private final RuleNodeExecution ruleNodeExecution;

  private boolean error;

  @Setter
  private long startTimestamp;

  @Setter
  private long endTimestamp;

  public RuleExecution(RuleNode<?> ruleNode, RuleNodeExecution ruleNodeExecution) {
    this.ruleNode = ruleNode;
    this.ruleNodeExecution = ruleNodeExecution;
    Throwable throwable = ruleNodeExecution.getThrowable();
    this.error = isError(throwable);
  }

  private boolean isError(Throwable throwable) {
    if (throwable == null) return false;

    return !(rootCause(throwable) instanceof ExecutionInterruptionException);
  }

  private Throwable rootCause(Throwable throwable) {
    Throwable rootCause = throwable;
    while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
      rootCause = rootCause.getCause();
    }
    return rootCause;
  }

  public RuleExecution(RuleNode<?> ruleNode, RuleNodeExecution ruleNodeExecution, Throwable t) {
    this(ruleNode, ruleNodeExecution);
    this.error = isError(t);
  }

    public long getTimeInMs() {
    return endTimestamp - startTimestamp;
  }

}
