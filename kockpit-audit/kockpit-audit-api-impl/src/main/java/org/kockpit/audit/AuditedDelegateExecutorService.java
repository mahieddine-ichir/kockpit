package org.kockpit.audit;

import java.util.concurrent.ExecutorService;

/**
 * Delegate {@link ExecutorService } to propagate audit report container instance from current
 * thread to child thread.
 */
public class AuditedDelegateExecutorService extends WrapDelegateExecutorService {

  public AuditedDelegateExecutorService(ExecutorService executorService) {
    super(executorService, new AuditChainExecutorCallWrapper());
  }

}
