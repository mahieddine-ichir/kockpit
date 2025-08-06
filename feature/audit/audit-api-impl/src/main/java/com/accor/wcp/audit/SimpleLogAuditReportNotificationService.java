package com.accor.wcp.audit;

import com.accor.wcp.audit.AuditReport.AuditJsonReport;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleLogAuditReportNotificationService implements AuditReportNotificationService {

  @Override
  public void notify(List<AuditJsonReport> auditReports) {
    auditReports.forEach(
        auditReport -> log.debug("Notify auditReport json: {}", auditReport.getAuditJson()));
  }
}
