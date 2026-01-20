package org.kockpit.audit.api;

public interface AuditorService {
  void startAudit();

  boolean isAuditStarted();

  void stopAuditAndNotify();

  AuditReport stopAudit();

  void notify(AuditReport auditReportData);
}
