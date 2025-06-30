package org.kockpit.rules.action;

import org.kockpit.rules.Action;
import org.kockpit.rules.DocumentationDetails;
import org.kockpit.rules.ExecutionInterruptionException;

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
