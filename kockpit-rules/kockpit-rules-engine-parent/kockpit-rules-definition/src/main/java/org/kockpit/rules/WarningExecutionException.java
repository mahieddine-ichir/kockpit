package org.kockpit.rules;

import org.kockpit.rules.execution.StepExecution;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
}
