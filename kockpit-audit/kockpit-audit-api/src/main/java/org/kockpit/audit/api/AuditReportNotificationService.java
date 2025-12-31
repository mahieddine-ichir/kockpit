package org.kockpit.audit.api;

import java.util.List;

public interface AuditReportNotificationService {

  /**
   * Send audit reports to inherent systems (brokers).
   * @param auditReport
   */
  void notify(List<AuditReport.AuditJsonReport> auditReport);
}
