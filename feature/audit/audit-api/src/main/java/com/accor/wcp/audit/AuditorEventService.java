package com.accor.wcp.audit;

import java.util.List;

/** Service to add audit event to "audit container". */
public interface AuditorEventService {
  void addAuditEvents(String type, List<AuditEvent> auditEvents);

  void addAuditEvents(String type, AuditEventsComputeFunction computeFunction);
}
