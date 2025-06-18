package org.kockpit.audit.stream.slf4j;

import org.kockpit.audit.stream.api.AuditConsumer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class Slf4jAuditConsumerConfiguration {

    @Bean("slf4j")
    public AuditConsumer auditConsumer() {
        return new Slf4jLogger();
    }
}
