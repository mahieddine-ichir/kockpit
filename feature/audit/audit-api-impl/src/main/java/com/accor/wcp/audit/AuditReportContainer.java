package com.accor.wcp.audit;

import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;

@Slf4j
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
      log.warn("Audit Report not started. To avoid functional issues, we don't throw exception but returning a local fake audit report.",
              new Exception("Getting audit report without starting audit. Exception to get stack trace."));
      return AuditReport.builder().build();
    }
    return AUDIT_REPORT_HOLDER.get();
  }
}
