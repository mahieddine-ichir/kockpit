package com.kockpit.rules.execution;

import com.kockpit.rules.RuleNode;

import java.util.LinkedList;
import java.util.List;

public class RuleNodeExecution {

  private final RuleNode ruleNode;

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

  public RuleNode getRuleNode() {
    return ruleNode;
  }

  public List<StepExecution> getStepExecutions() {
    return stepExecutions;
  }

  public void setStepExecutions(List<StepExecution> stepExecutions) {
    this.stepExecutions = stepExecutions;
  }

  public boolean isOk() {
    return ok;
  }

  public void setOk(boolean ok) {
    this.ok = ok;
  }

  public RuleNodeExecution next() {
    return next;
  }

  public RuleNodeExecution getNext() {
    return next;
  }

  public void setNext(RuleNodeExecution next) {
    this.next = next;
  }

  public void setThrowable(Throwable throwable) {
    this.throwable = throwable;
  }

  public Throwable getThrowable() {
    return throwable;
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

  public boolean isWarning() {
    return warning;
  }

  public void setWarning(boolean warning) {
    this.warning = warning;
  }

  public boolean isSkipped() {
    return skipped;
  }

  public void setSkipped(boolean skipped) {
    this.skipped = skipped;
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
