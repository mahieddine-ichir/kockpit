package org.kockpit.rules.executor;

import org.kockpit.rules.Action;
import org.kockpit.rules.ActionExecutor;

public class DefaultActionExecutor<T> implements ActionExecutor<T> {
  @Override
  public void execute(Action<T> action, T context) throws Exception {
    action.execute(context);
  }
}
