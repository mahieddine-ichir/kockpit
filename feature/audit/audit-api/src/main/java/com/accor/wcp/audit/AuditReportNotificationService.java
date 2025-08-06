package com.accor.wcp.audit;

import com.accor.wcp.audit.AuditReport.AuditJsonReport;
import java.util.Collections;
import java.util.List;

public interface AuditReportNotificationService {
  void notify(List<AuditJsonReport> auditReport);

  default void notify(AuditJsonReport auditReport) {
    this.notify(Collections.singletonList(auditReport));
  }
}
