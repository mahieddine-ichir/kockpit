package com.kockpit.rules.execution;

import com.kockpit.rules.RuleNodeException;
import com.kockpit.rules.WarningExecutionException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExecutionResult {
  private String executionId;

  private List<RuleExecution> ruleExecutions;

  private boolean successful;

  private boolean warning;

  private RuleNodeException throwable;

  private final List<WarningExecutionException> warnings = new ArrayList<>();

  private long startTimestamp;

  private long endTimestamp;

  private StepExecution errorStepExecution;

  private StepExecution warningStepExecution;

  public ExecutionResult(List<RuleExecution> ruleExecutions) {
    this.executionId = UUID.randomUUID().toString();
    this.ruleExecutions = ruleExecutions;
    startTimestamp = System.currentTimeMillis();
  }

  public List<RuleExecution> getRuleExecutions() {
    return ruleExecutions;
  }

  public boolean isSuccessful() {
    return successful;
  }

  public void setSuccessful(boolean successful) {
    this.successful = successful;
  }

  public RuleNodeException getThrowable() {
    return throwable;
  }

  public void setThrowable(RuleNodeException throwable) {
    this.throwable = throwable;
  }

  public List<WarningExecutionException> getWarnings() {
    return warnings;
  }

  public void addWarning(WarningExecutionException e) {
    warnings.add(e);
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

  public long getTimeInMs() {
    return endTimestamp - startTimestamp;
  }

  public void setErrorStepExecution(StepExecution errorStepExecution) {
    this.errorStepExecution = errorStepExecution;
  }

  public StepExecution getErrorStepExecution() {
    return errorStepExecution;
  }

  public StepExecution getWarningStepExecution() {
    return warningStepExecution;
  }

  public void setWarningStepExecution(StepExecution warningStepExecution) {
    this.warningStepExecution = warningStepExecution;
  }

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
}
