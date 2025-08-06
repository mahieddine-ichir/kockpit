package com.accor.wcp.audit;

import java.util.List;

public interface Audit {
  String getType();

  List<AuditEvent> getEvents();
}
