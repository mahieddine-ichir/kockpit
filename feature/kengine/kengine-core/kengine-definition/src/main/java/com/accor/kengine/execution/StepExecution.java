package com.accor.kengine.execution;

import com.accor.kengine.DocumentationDetails;
import com.accor.kengine.WarningExecutionException;

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

  public StepType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public String getClassname() {
    return classname;
  }

  public boolean isExecutionError() {
    return executionError;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public boolean isResult() {
    return result;
  }

  public DocumentationDetails getDetails() {
    return details;
  }

  public Throwable getThrowable() {
    return throwable;
  }

  public void setThrowable(Throwable throwable) {
    this.executionError = true;
    this.throwable = throwable;
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

  public boolean isExecutionWarning() {
    return executionWarning;
  }

  public void setExecutionWarning(boolean executionWarning) {
    this.executionWarning = executionWarning;
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
