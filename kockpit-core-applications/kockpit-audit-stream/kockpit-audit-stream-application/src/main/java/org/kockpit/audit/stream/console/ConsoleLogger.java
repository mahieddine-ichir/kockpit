package org.kockpit.audit.stream.console;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.kockpit.audit.stream.api.AuditStreamJson;

import java.util.List;

@Slf4j
public class ConsoleLogger implements AuditConsumer {

    @PostConstruct
    public void start() {
        log.trace("Console Audit consumer started!");
    }

    @Override
    public void accept(List<byte[]> byteBuffers) {
        byteBuffers.forEach(byteBuffer -> log.trace("{}", AuditStreamJson.readAuditReport(byteBuffer)));
    }
}
