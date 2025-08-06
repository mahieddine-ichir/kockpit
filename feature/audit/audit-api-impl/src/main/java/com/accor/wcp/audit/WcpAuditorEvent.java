package com.accor.wcp.audit;

import static java.util.Collections.synchronizedList;

import java.util.ArrayList;
import java.util.List;

class WcpAuditorEvent implements AuditorEventService {

  @Override
  public void addAuditEvents(String type, List<AuditEvent> auditEvents) {
    AuditImpl audit = getAudit(type);
    audit.addEvents(auditEvents);
  }

  private static AuditImpl getAudit(String type) {
    return (AuditImpl)
        AuditReportContainer.getAuditReport()
            .getAuditsMap()
            .computeIfAbsent(
                type,
                s ->
                    new AuditImpl(
                        s,
                        synchronizedList(new ArrayList<>()),
                        synchronizedList(new ArrayList<>())));
  }

  @Override
  public void addAuditEvents(String type, AuditEventsComputeFunction computeFunction) {
    getAudit(type).addComputeFunction(computeFunction);
  }

  static void closeCurrentAuditEvents() {
    // Late compute of audit events
    AuditReport auditReport = AuditReportContainer.getAuditReport();
    auditReport.getAudits().forEach(audit -> ((AuditImpl) audit).close());
  }
}
