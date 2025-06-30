package org.kockpit.rules;

public interface ActionExecutor<T> {

  void execute(Action<T> action, T context) throws Exception;
}
