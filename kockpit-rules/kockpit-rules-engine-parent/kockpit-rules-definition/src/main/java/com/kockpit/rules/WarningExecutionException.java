package com.kockpit.rules;

import com.kockpit.rules.execution.StepExecution;

public class WarningExecutionException extends RuntimeException {

  private boolean stopExecution;

  private StepExecution stepExecution;

  public WarningExecutionException(String s) {
    super(s);
  }

  public WarningExecutionException(String s, Throwable throwable) {
    super(s, throwable);
  }

  public WarningExecutionException(String s, Boolean stopExecution) {
    super(s);
    this.stopExecution = stopExecution;
  }

  public WarningExecutionException(String s, Throwable throwable, Boolean stopExecution) {
    super(s, throwable);
    this.stopExecution = stopExecution;
  }

  public boolean isStopExecution() {
    return stopExecution;
  }

  public StepExecution getStepExecution() {
    return stepExecution;
  }

  public void setStepExecution(StepExecution stepExecution) {
    this.stepExecution = stepExecution;
  }
}
