package com.accor.wcp.services.auditstream.notification.http;

import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.accor.wcp.services.auditstream.notification.service.AuditReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationEndpoint {

  private final AuditReportService auditReportService;

  @PostMapping(
      value = "/notification",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public void createAuditRequest(@RequestBody AuditReportRequest auditReportRequest) {
    auditReportService.report(auditReportRequest);
  }
}
