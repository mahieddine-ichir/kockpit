package org.kockpit.audit;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReport;
import org.kockpit.audit.api.AuditReportNotificationService;

import java.util.List;

@Slf4j
public class SimpleLogNotificationService implements AuditReportNotificationService {
    @Override
    public void notify(List<AuditReport.AuditJsonReport> auditReport) {
      auditReport.forEach(auditJsonReport -> log.info(auditJsonReport.toString()));
    }
}
