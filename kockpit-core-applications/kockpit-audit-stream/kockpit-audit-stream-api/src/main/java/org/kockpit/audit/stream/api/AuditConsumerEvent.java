package org.kockpit.audit.stream.api;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

public class AuditConsumerEvent extends ApplicationEvent {

    @Getter
    private final List<byte[]> data;

    public AuditConsumerEvent(Object source, List<byte[]> data) {
        super(source);
        this.data = data;
    }
}
