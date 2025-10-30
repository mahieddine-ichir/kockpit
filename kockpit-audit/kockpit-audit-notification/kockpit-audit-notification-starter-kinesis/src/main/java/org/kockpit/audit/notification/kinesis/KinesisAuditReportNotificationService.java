package org.kockpit.audit.notification.kinesis;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReport.AuditJsonReport;
import org.kockpit.audit.api.AuditReportNotificationService;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;

import java.util.List;

@Slf4j
record KinesisAuditReportNotificationService(
        KinesisAsyncClient kinesisAsyncClient
) implements AuditReportNotificationService {

    @Override
    public void notify(List<AuditJsonReport> auditReports) {
        this.publishEvents(auditReports);
    }

    void publishEvents(List<AuditJsonReport> auditReports) {

    }
}
