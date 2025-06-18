package com.kockpit.rules.execution;

import com.kockpit.rules.ExecutionInterruptionException;
import com.kockpit.rules.RuleNode;

public class RuleExecution {

  private final RuleNode ruleNode;

  private final RuleNodeExecution ruleNodeExecution;

  private boolean error;

  private long startTimestamp;

  private long endTimestamp;

  public RuleExecution(RuleNode ruleNode, RuleNodeExecution ruleNodeExecution) {
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

  public RuleExecution(RuleNode ruleNode, RuleNodeExecution ruleNodeExecution, Throwable t) {
    this(ruleNode, ruleNodeExecution);
    this.error = isError(t);
  }

  public RuleNode getRuleNode() {
    return ruleNode;
  }

  public RuleNodeExecution getRuleNodeExecution() {
    return ruleNodeExecution;
  }

  public boolean isError() {
    return error;
  }

  public long getTimeInMs() {
    return endTimestamp - startTimestamp;
  }

  public long getStartTimestamp() {
    return startTimestamp;
  }

  public void setStartTimestamp(long startTimestamp) {
    this.startTimestamp = startTimestamp;
  }

  public long getEndTimestamp() {
    return endTimestamp;
  }

  public void setEndTimestamp(long endTimestamp) {
    this.endTimestamp = endTimestamp;
  }
}
