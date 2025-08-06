package com.accor.kengine;

public class DefaultActionExecutor<T> implements ActionExecutor<T> {
  @Override
  public void execute(Action<T> action, T context) throws Exception {
    action.execute(context);
  }
}
