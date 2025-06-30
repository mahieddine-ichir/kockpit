package org.kockpit.rules;

import org.kockpit.rules.execution.StepExecution;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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

}
