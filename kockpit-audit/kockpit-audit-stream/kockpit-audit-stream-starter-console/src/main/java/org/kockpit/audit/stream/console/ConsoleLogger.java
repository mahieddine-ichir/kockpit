package org.kockpit.audit.stream.console;

import org.kockpit.audit.stream.api.AuditConsumer;
import org.kockpit.audit.stream.api.model.AuditReport;

public class ConsoleLogger implements AuditConsumer {

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void accept(AuditReport o) {
        System.out.println(""+o);
    }
}
