package org.kockpit.audit;

import org.kockpit.audit.api.AuditReport;

import java.util.concurrent.Callable;

/**
 * Abstract class to propagate audit report container instance from current thread to child thread.
 */
abstract class AbstractAuditedDelegateExecutor {

  protected <T> Callable<T> wrap(Callable<T> task) {
    AuditReport parentAuditReport = AuditReportContainer.AUDIT_REPORT_HOLDER.get();
    return () -> {
      AuditReportContainer.AUDIT_REPORT_HOLDER.set(parentAuditReport);
      T call;
      try {
        call = task.call();
      } finally {
        AuditReportContainer.AUDIT_REPORT_HOLDER.remove();
      }
      return call;
    };
  }

  protected Runnable wrap(Runnable task) {
    AuditReport parentAuditReport = AuditReportContainer.AUDIT_REPORT_HOLDER.get();
    return () -> {
      AuditReportContainer.AUDIT_REPORT_HOLDER.set(parentAuditReport);
      try {
        task.run();
      } finally {
        AuditReportContainer.AUDIT_REPORT_HOLDER.remove();
      }
    };
  }
}
