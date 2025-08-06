package com.accor.wcp.audit.module.web.response;

import com.accor.wcp.audit.IndexedKeyValue;
import com.accor.wcp.audit.module.web.ResponseAuditor;
import com.accor.wcp.audit.module.web.WebAuditReportData;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.ContentCachingResponseWrapper;

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
