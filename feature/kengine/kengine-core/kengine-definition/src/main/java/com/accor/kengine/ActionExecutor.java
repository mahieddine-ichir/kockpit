package com.accor.kengine;

public interface ActionExecutor<T> {

  void execute(Action<T> action, T context) throws Exception;
}
