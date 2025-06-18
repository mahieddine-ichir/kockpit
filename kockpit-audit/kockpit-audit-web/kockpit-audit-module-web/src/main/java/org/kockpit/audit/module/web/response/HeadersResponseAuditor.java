package org.kockpit.audit.module.web.response;

import org.kockpit.audit.api.IndexedKeyValue;
import org.kockpit.audit.module.web.ResponseAuditor;
import org.kockpit.audit.module.web.WebAuditReportData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.util.List;

@RequiredArgsConstructor
public class HeadersResponseAuditor implements ResponseAuditor {

  private final List<String> headerNamesToAudit;

  @Override
  public void audit(ContentCachingResponseWrapper response, WebAuditReportData webAuditReport) {
    headerNamesToAudit.forEach(
        requestHeaderToAudit -> auditHeaderValue(requestHeaderToAudit, response, webAuditReport));
  }

  private void auditHeaderValue(
      String headerName,
      ContentCachingResponseWrapper response,
      WebAuditReportData webAuditReport) {
    webAuditReport
        .getIndexedKeyValues()
        .add(IndexedKeyValue.of(headerName, response.getHeader(headerName)));
  }
}
