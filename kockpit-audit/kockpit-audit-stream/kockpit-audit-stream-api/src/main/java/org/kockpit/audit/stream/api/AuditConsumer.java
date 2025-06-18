package org.kockpit.audit.stream.api;

import java.util.function.Consumer;

public interface AuditConsumer extends Consumer<AuditReport> {

    void start();
    void stop();

    void onError(Throwable throwable);
}
