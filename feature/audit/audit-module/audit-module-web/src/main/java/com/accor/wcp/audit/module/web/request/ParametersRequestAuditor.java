package com.accor.wcp.audit.module.web.request;

import com.accor.wcp.audit.IndexedKeyValue;
import com.accor.wcp.audit.module.web.RequestAuditor;
import com.accor.wcp.audit.module.web.WebAuditReportData;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.ContentCachingRequestWrapper;

@RequiredArgsConstructor
public class ParametersRequestAuditor implements RequestAuditor {

  @Value("${wcp.sdk.service.audit.web.http-parameters}")
  private List<String> paramsNameToAudit;

  @Override
  public void audit(ContentCachingRequestWrapper request, WebAuditReportData webAuditReport) {
    request
        .getParameterMap()
        .forEach((key, strings) -> addWebAuditReportIfParamsToAudit(key, strings, webAuditReport));
  }

  private void addWebAuditReportIfParamsToAudit(
      String key, String[] strings, WebAuditReportData webAuditReport) {
    if (paramsNameToAudit.contains(key)) {
      webAuditReport.getIndexedKeyValues().add(IndexedKeyValue.of(key, String.join(",", strings)));
    }
  }
}
