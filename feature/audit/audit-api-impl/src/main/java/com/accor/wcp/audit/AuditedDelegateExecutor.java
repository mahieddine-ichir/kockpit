package com.accor.wcp.audit;

import java.util.concurrent.*;

/**
 * Executor Delegate to propagate audit report container instance from current thread to child
 * thread.
 */
public class AuditedDelegateExecutor extends AbstractAuditedDelegateExecutor implements Executor {
  private final Executor executor;

  public AuditedDelegateExecutor(Executor executor) {
    this.executor = executor;
  }

  @Override
  public void execute(Runnable command) {
    executor.execute(this.wrap(command));
  }
}
