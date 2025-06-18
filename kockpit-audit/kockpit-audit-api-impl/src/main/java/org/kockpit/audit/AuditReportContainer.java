package org.kockpit.audit;

import org.kockpit.audit.api.AuditNotStartedException;
import org.kockpit.audit.api.AuditReport;
import org.springframework.core.NamedThreadLocal;

import java.util.Objects;

public class AuditReportContainer {

  private AuditReportContainer() {}

  static final ThreadLocal<AuditReport> AUDIT_REPORT_HOLDER =
      new NamedThreadLocal<>("Audit Report Container");

  static void resetReport() {
    AUDIT_REPORT_HOLDER.remove();
  }

  static boolean isAuditStarted() {
    return Objects.nonNull(AUDIT_REPORT_HOLDER.get());
  }

  static void setAuditReport(AuditReport auditReport) {
    if (auditReport == null) {
      resetReport();
    } else {
      AUDIT_REPORT_HOLDER.set(auditReport);
    }
  }

  static AuditReport getAuditReport() {
    if (!isAuditStarted()) {
      throw new AuditNotStartedException();
    }
    return AUDIT_REPORT_HOLDER.get();
  }
}
