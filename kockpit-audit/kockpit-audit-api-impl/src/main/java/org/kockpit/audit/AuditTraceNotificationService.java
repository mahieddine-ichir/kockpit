package org.kockpit.audit;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.kockpit.audit.api.AuditReportWrapper;

import java.util.List;

@Slf4j
public class AuditTraceNotificationService implements AuditReportNotificationService {

    @Override
    public void notify(List<AuditReportWrapper> auditReports) {
      auditReports.forEach(report -> log.trace(new String(report.data())));
    }
}
