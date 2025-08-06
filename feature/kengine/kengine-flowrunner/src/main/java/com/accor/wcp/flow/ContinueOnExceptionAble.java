package com.accor.wcp.flow;

/** Feature interface to decide if flow execution should stop or continue. */
public interface ContinueOnExceptionAble {
  boolean shouldContinueOnException();
}
