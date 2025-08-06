package com.accor.kengine.action;

import com.accor.kengine.Action;
import com.accor.kengine.DocumentationDetails;
import com.accor.kengine.ExecutionInterruptionException;

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
