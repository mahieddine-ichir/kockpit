package com.example.insightsstreamingapp.config;

import org.kockpit.audit.stream.api.AuditConsumer;
import org.kockpit.audit.stream.api.AuditReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaInsightsConsumer {

    private final AuditConsumer insightsConsumer;

    @Autowired
    public KafkaInsightsConsumer(
            @Qualifier("opensearch-insights") AuditConsumer insightsConsumer
    ) {
        this.insightsConsumer = insightsConsumer;
    }

    @KafkaListener(
            topics = "${kockpit.audit.stream.kafka.topics:audit}",
            groupId = "${spring.kafka.consumer.group-id:insights-group}"
    )
    public void consumeAudit(AuditReport report) {
        insightsConsumer.accept(report);
    }
}
