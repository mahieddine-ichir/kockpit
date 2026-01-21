package org.kockpit.audit.api;

import java.util.List;

public interface AuditReportNotificationService {

  /**
   * Send audit reports to inherent systems (brokers).
   * @param auditReports audit reports to notify/send to inherent systems
   */
  void notify(List<AuditReportWrapper> auditReports);
}
