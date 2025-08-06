package com.accor.wcp.audit;

public interface AuditorService {
  void startAudit();

  void startAudit(Integer ttl);

  boolean isAuditStarted();

  void stopAuditAndNotify();

  AuditReport stopAudit();

  void notify(AuditReport auditReportData);
}
