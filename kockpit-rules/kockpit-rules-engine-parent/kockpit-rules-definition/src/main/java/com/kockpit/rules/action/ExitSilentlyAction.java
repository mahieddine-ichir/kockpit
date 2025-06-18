package com.kockpit.rules.action;

import com.kockpit.rules.Action;
import com.kockpit.rules.DocumentationDetails;
import com.kockpit.rules.ExecutionInterruptionException;

public class ExitSilentlyAction<T> implements Action<T> {
  private final String exitSilentlyMessage;
  private final DocumentationDetails details;

  public ExitSilentlyAction(String exitSilentlyMessage, DocumentationDetails details) {
    this.exitSilentlyMessage = exitSilentlyMessage;
    this.details = details;
  }

  @Override
  public void execute(T context) throws Exception {
    throw new ExecutionInterruptionException(exitSilentlyMessage);
  }

  @Override
  public DocumentationDetails getDetails() {
    return details;
  }
}
