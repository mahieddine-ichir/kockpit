package org.kockpit.audit.api;

import java.util.Collections;
import java.util.List;

public interface AuditReportNotificationService {
  void notify(List<AuditReport.AuditJsonReport> auditReport);

  default void notify(AuditReport.AuditJsonReport auditReport) {
    this.notify(Collections.singletonList(auditReport));
  }
}
