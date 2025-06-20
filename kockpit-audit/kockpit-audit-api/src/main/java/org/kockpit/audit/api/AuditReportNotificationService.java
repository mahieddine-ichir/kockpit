package org.kockpit.audit.api;

import java.util.List;

public interface AuditReportNotificationService {
  void notify(List<AuditReport.AuditJsonReport> auditReport);
}
