package com.accor.kengine;

import com.accor.kengine.execution.StepExecution;

public class ErrorExecutionException extends RuntimeException {

  private StepExecution stepExecution;

  public ErrorExecutionException(String s) {
    super(s);
  }

  public ErrorExecutionException(String s, Throwable throwable) {
    super(s, throwable);
  }

  public ErrorExecutionException(String s, StepExecution stepExecution) {
    super(s);
    this.stepExecution = stepExecution;
  }

  public ErrorExecutionException(String s, Throwable throwable, StepExecution stepExecution) {
    super(s, throwable);
    this.stepExecution = stepExecution;
  }

  public StepExecution getStepExecution() {
    return stepExecution;
  }

  public void setStepExecution(StepExecution stepExecution) {
    this.stepExecution = stepExecution;
  }
}
