package org.kockpit.audit.backoffice.DataModel;

import java.util.List;


public interface Audit {
    String getType();
    List<AuditEvent> getEvents();
}
