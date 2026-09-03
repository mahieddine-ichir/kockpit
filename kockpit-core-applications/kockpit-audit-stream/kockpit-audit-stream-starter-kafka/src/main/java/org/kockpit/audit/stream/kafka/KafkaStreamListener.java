package org.kockpit.audit.stream.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class KafkaStreamListener {

    private final List<AuditConsumer> auditConsumers;

    @KafkaListener(topics = "${kockpit.audit.stream.kafka.topics}")
    void processAudit(List<byte[]> messages) {
        try {
            auditConsumers.forEach(auditConsumer -> auditConsumer.accept(messages));
        } catch (Exception e) {
            log.error("Error processing audit messages", e);
        }
    }
}
