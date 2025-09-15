package org.kockpit.audit.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.kockpit.audit.stream.api.model.AuditReport;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableScheduling
public class AuditConsuming {

    private final List<AuditConsumer> auditConsumers;

    private final ObjectMapper objectMapper;

    @PostConstruct
    void startConsumers(
    ) {
        auditConsumers.forEach(AuditConsumer::start);
    }

    @PreDestroy
    void stopConsumers() {
        auditConsumers.forEach(AuditConsumer::stop);
    }

    @KafkaListener(topics = "${kockpit.audit.stream.kafka.topics}")
    void processAudit(String event) {
            try {
                AuditReport audit = objectMapper.readValue(event, AuditReport.class);
                auditConsumers.forEach(auditConsumer -> auditConsumer.accept(audit));
            } catch (Exception e) {
                log.error("Error deserializing event {}. Error: {}", event, e.getMessage(), e);
            }
    }
}
