package org.kockpit.audit;

import org.kockpit.audit.api.AuditReport;

/** Audit obfuscate service integration. */
public interface AuditObfuscationService {
  void obfuscate(AuditReport auditReport);
}
