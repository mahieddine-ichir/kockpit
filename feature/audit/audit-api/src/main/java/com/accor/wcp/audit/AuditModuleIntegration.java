package com.accor.wcp.audit;

import java.util.List;

/** Audit module integration definition. */
public interface AuditModuleIntegration {
  String supportedType();

  void postProcessAuditEvents(List<AuditEvent> events);
}
