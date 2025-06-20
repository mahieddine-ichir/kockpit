package org.kockpit.audit.backend.model;

import java.util.List;


public interface Audit {
    String getType();
    List<AuditEvent> getEvents();
}
