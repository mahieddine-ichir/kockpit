package org.kockpit.audit.stream.console;

import org.kockpit.audit.stream.api.AuditConsumer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ConsoleAuditConsumerConfiguration {

    @Bean("console")
    public AuditConsumer auditConsumer() {
        return new ConsoleLogger();
    }
}
