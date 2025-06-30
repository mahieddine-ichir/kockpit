package org.kockpit.audit.module.web.request;

import org.kockpit.audit.api.IndexedKeyValue;
import org.kockpit.audit.module.web.RequestAuditor;
import org.kockpit.audit.module.web.WebAuditReportData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.util.List;

@RequiredArgsConstructor
public class ParametersRequestAuditor implements RequestAuditor {

  @Value("${kockpit.sdk.service.audit.web.http-parameters}")
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
