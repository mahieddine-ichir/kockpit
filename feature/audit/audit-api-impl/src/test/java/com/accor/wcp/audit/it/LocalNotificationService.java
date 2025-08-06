package com.accor.wcp.audit.it;

import com.accor.wcp.audit.AuditReport.AuditJsonReport;
import com.accor.wcp.audit.AuditReportNotificationService;
import java.util.List;
import lombok.Data;

@Data
public class LocalNotificationService implements AuditReportNotificationService {

  List<AuditJsonReport> auditReport;

  @Override
  public void notify(List<AuditJsonReport> auditReport) {
    this.auditReport = auditReport;
  }
}
