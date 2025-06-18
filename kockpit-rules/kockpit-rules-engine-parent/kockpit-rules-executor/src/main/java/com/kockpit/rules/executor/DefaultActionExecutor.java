package com.kockpit.rules.executor;

import com.kockpit.rules.Action;
import com.kockpit.rules.ActionExecutor;

public class DefaultActionExecutor<T> implements ActionExecutor<T> {
  @Override
  public void execute(Action<T> action, T context) throws Exception {
    action.execute(context);
  }
}
