package org.kockpit.audit.module.web.request;

import org.kockpit.audit.api.IndexedKeyValue;
import org.kockpit.audit.module.web.RequestAuditor;
import org.kockpit.audit.module.web.WebAuditReportData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.util.List;

@RequiredArgsConstructor
public class HeadersRequestAuditor implements RequestAuditor {

  private final List<String> headerNamesToAudit;

  @Override
  public void audit(ContentCachingRequestWrapper request, WebAuditReportData webAuditReport) {
    headerNamesToAudit.forEach(
        requestHeaderToAudit -> auditHeaderValue(requestHeaderToAudit, request, webAuditReport));
  }

  private void auditHeaderValue(
      String headerName, ContentCachingRequestWrapper request, WebAuditReportData webAuditReport) {
    webAuditReport
        .getIndexedKeyValues()
        .add(IndexedKeyValue.of(headerName, request.getHeader(headerName)));
  }
}
