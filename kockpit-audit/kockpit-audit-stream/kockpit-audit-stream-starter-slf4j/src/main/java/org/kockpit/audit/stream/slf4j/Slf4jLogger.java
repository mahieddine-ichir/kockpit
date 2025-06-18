package org.kockpit.audit.stream.slf4j;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.kockpit.audit.stream.api.AuditReport;

@Slf4j
public class Slf4jLogger implements AuditConsumer {

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("{}", throwable.getMessage(), throwable);
    }

    @Override
    public void accept(AuditReport o) {
        log.info("{}", o);
    }
}
