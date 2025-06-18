package org.kockpit.audit.api;

import java.util.List;

public interface Audit {
  String getType();

  List<AuditEvent> getEvents();
}
