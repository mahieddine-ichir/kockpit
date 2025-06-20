package org.kockpit.audit;

import org.kockpit.audit.api.AuditEvent;
import org.kockpit.audit.api.AuditEventsComputeFunction;
import org.kockpit.audit.api.AuditReport;
import org.kockpit.audit.api.AuditorEventService;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.synchronizedList;

class KockpitAuditorEvent implements AuditorEventService {

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
