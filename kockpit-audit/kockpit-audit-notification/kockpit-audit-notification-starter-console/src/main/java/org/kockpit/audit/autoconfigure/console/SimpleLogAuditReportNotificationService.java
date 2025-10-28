package org.kockpit.audit.autoconfigure.console;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReport.AuditJsonReport;
import org.kockpit.audit.api.AuditReportNotificationService;

import java.util.List;

@Slf4j
public class SimpleLogAuditReportNotificationService implements AuditReportNotificationService {

  @Override
  public void notify(List<AuditJsonReport> auditReports) {
    auditReports.forEach(
        auditReport -> log.trace("Notify auditReport json: {}", auditReport.getAuditJson()));
  }
}
