package com.accor.kengine;

@Deprecated
@FunctionalInterface
public interface Action<T> extends ActionDetails {

  void execute(T context) throws Exception;

  default void execute(T context, ExecutionContext executionContext) throws Exception {
    execute(context);
  }

  // FIXME - remove this default method (when reviewing engine model / definition)
  default DocumentationDetails getDetails() {
    return null;
  }
}
