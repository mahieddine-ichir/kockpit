package org.kockpit.audit.api;

public interface AuditorService {
  void startAudit();

  boolean isAuditStarted();

  void stopAuditAndNotify();

  AuditReport stopAudit();

  void notify(AuditReport auditReportData);

  /**
   * Returns the current audit report bound to this thread, or {@code null} if no audit is started.
   * Intended for capturing the audit context before entering a reactive chain where the original
   * thread-local will no longer be available.
   */
  AuditReport currentAuditReport();

  /**
   * Temporarily binds {@code report} to the current thread, runs {@code task}, then clears the
   * binding. Does nothing if {@code report} is {@code null}. Use this to restore audit context
   * inside reactive callbacks (e.g. WebClient {@code ExchangeFilterFunction}).
   */
  void executeWithAuditReport(AuditReport report, Runnable task);
}
