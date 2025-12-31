package org.kockpit.audit.notification.kafka;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReport.AuditJsonReport;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.kockpit.audit.api.CompressionService;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

@Slf4j
record KafkaAuditReportNotificationService(
        KafkaTemplate<String, byte[]> producerClient,
        String topic,
        CompressionService compressionService) implements AuditReportNotificationService {

    @Override
    public void notify(List<AuditJsonReport> auditReports) {
        this.publishEvents(auditReports);
    }

    void publishEvents(List<AuditJsonReport> auditReports) {
        // sample events in an array
        auditReports
                .stream()
                .map(AuditJsonReport::getAuditJson)
                .map(compressionService::compress)
                .forEach(compressedData -> producerClient.send(topic, compressedData));
    }
}
