package org.kockpit.rules.action;

import org.kockpit.rules.Action;
import org.kockpit.rules.DocumentationDetails;

import java.util.function.Function;

public class ExitWithErrorAction<T> implements Action<T> {
  private final String exitErrorMessage;
  private final DocumentationDetails details;
  private final Function<T, Exception> dynamicException;

  public ExitWithErrorAction(
      Function<T, Exception> dynamicException,
      String exitErrorMessage,
      DocumentationDetails details) {
    this.dynamicException = dynamicException;
    this.exitErrorMessage = exitErrorMessage;
    this.details = details;
  }

  public ExitWithErrorAction(
      Function<T, Exception> dynamicException, DocumentationDetails details) {
    this.details = details;
    this.dynamicException = dynamicException;
    this.exitErrorMessage = null;
  }

  @Override
  public void execute(T context) throws Exception {
    throw dynamicException.apply(context);
  }

  @Override
  public DocumentationDetails getDetails() {
    return details;
  }
}
