package org.kockpit.audit.backend.DataModel;

import java.util.List;


public interface Audit {
    String getType();
    List<AuditEvent> getEvents();
}
