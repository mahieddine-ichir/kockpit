package org.kockpit.audit.stream.api;

import org.kockpit.audit.stream.api.model.AuditReport;
import org.springframework.context.ApplicationEvent;

public class AuditConsumerEvent extends ApplicationEvent {
    public AuditConsumerEvent(AuditReport auditReport) {
        super(auditReport);
    }
}
