package org.kockpit.rules.execution;

import org.kockpit.rules.DocumentationDetails;
import org.kockpit.rules.WarningExecutionException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StepExecution {

  private StepType type;

  private String name;

  private String classname;

  private boolean executionError;

  private boolean executionWarning;

  private Throwable throwable;

  private String errorMessage;

  private boolean result;

  private DocumentationDetails details;

  private long startTimestamp;

  private long endTimestamp;

  public StepExecution(
      StepType type, String name, String classname, boolean result, DocumentationDetails details) {
    this.type = type;
    this.name = name;
    this.classname = classname;
    this.result = result;
    this.details = details;
  }

  public StepExecution(
      StepType type,
      String name,
      String classname,
      Throwable throwable,
      String errorMessage,
      DocumentationDetails details) {
    this.type = type;
    this.name = name;
    this.classname = classname;
    this.throwable = throwable;
    this.executionError = true;
    this.errorMessage = errorMessage;
    this.details = details;
  }

  public StepExecution(
      StepType type,
      String name,
      String classname,
      WarningExecutionException warningException,
      String errorMessage,
      DocumentationDetails details) {
    this.type = type;
    this.name = name;
    this.classname = classname;
    this.throwable = warningException;
    this.executionError = false;
    this.executionWarning = true;
    this.errorMessage = errorMessage;
    this.details = details;
  }

    public void setThrowable(Throwable throwable) {
    this.executionError = true;
    this.throwable = throwable;
  }

  public long getTimeInMs() {
    return endTimestamp - startTimestamp;
  }

  @Override
  public String toString() {
    return "StepExecution{"
        + "type="
        + type
        + ", name='"
        + name
        + '\''
        + ", classname='"
        + classname
        + '\''
        + ", executionError="
        + executionError
        + ", errorMessage='"
        + errorMessage
        + '\''
        + ", result="
        + result
        + ", details="
        + details
        + '}';
  }
}
