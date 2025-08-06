package com.accor.wcp.audit.notification.http;

import com.accor.wcp.audit.AuditReport.AuditJsonReport;
import com.accor.wcp.audit.AuditReportNotificationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestTemplate;

public class HttpAuditReportNotificationService implements AuditReportNotificationService {

  private final String auditServiceUrl;

  @Qualifier("auditRestemplate")
  private final RestTemplate restTemplate;

  public HttpAuditReportNotificationService(String auditServiceUrl, RestTemplate restTemplate) {
    this.auditServiceUrl = auditServiceUrl;
    this.restTemplate = restTemplate;
  }

  @Override
  public void notify(List<AuditJsonReport> auditReports) {
    auditReports.forEach(
        auditReport ->
            restTemplate.postForObject(auditServiceUrl, auditReport.getAuditReport(), Void.class));
  }
}
