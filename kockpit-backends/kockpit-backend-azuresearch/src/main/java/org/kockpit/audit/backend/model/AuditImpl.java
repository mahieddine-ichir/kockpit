package org.kockpit.audit.backend.model;

import java.util.List;

public class AuditImpl implements Audit {
    private String type;
    private List<AuditEvent> events;

    @Override
    public String getType() {
        return "";
    }

    @Override
    public List<AuditEvent> getEvents() {
        return List.of();
    }

}
