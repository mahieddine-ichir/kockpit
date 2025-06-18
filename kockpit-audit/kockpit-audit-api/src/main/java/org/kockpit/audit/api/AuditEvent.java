package org.kockpit.audit.api;

public interface AuditEvent {
  long getStartTime();

  long getEndTime();
}
