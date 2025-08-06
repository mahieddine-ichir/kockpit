package com.accor.wcp.audit.module.web.request;

import com.accor.wcp.audit.IndexedKeyValue;
import com.accor.wcp.audit.module.web.RequestAuditor;
import com.accor.wcp.audit.module.web.WebAuditReportData;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.ContentCachingRequestWrapper;

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
