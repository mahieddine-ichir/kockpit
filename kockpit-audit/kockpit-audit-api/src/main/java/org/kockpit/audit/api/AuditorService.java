package org.kockpit.audit.api;

public interface AuditorService {
  void startAudit();

  void startAudit(Integer ttl);

  // fixme check usage
  boolean isAuditStarted();

  void stopAuditAndNotify();

  AuditReport stopAudit();

  void notify(AuditReport auditReportData);
}
