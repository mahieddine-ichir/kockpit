package org.kockpit.audit.notification.kafka;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.kockpit.audit.api.AuditReportWrapper;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

@Slf4j
record KafkaAuditReportNotificationService(
        KafkaTemplate<String, byte[]> producerClient,
        String topic) implements AuditReportNotificationService {

    @Override
    public void notify(List<AuditReportWrapper> auditReports) {
        this.publishEvents(auditReports);
    }

    void publishEvents(List<AuditReportWrapper> auditReports) {
        auditReports.forEach(report -> producerClient.send(topic, key(report), report.data())
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Error sending record to kafka topic {}", topic, ex);
                    }
                }));
    }

    private String key(AuditReportWrapper report) {
        return "%s-%s-%s".formatted(report.domain(), report.env(), report.appId());
    }
}
