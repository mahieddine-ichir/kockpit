package com.accor.wcp.audit.module.web;

import static java.util.Objects.isNull;

import com.accor.wcp.audit.IndexedKeyValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

final class WebAuditKeyValues {

  /**
   * Utility class method.
   */
  private WebAuditKeyValues() {
  }

  public static List<IndexedKeyValue> getWebKeyValues(WebAuditReportData auditReport) {
    WebAuditEvent webAuditEvent = auditReport.getWebAuditEvent();
    if (isNull(webAuditEvent)) {
      return Collections.emptyList();
    }
    List<IndexedKeyValue> indexedKeyValues = new ArrayList<>();
    indexedKeyValues.addAll(
        List.of(
            IndexedKeyValue.of("requestUri", webAuditEvent.getHttpAuditedRequest().getUri()),
            IndexedKeyValue.of(
                "traceId", Optional.ofNullable(webAuditEvent.getTraceId()).orElse("")),
            IndexedKeyValue.of(
                "origin", Optional.ofNullable(webAuditEvent.getOrigin()).orElse("")),
            new IndexedKeyValue("httpStatus", String.valueOf(webAuditEvent.getHttpAuditedResponse().getStatus()),
                webAuditEvent.getHttpAuditedResponse().getStatus(), null, null),
            IndexedKeyValue.of("httpMethod", webAuditEvent.getHttpAuditedRequest().getMethod()),
            IndexedKeyValue.of(
                "principal", webAuditEvent.getHttpAuditedRequest().getPrincipal())));
    indexedKeyValues.addAll(auditReport.getIndexedKeyValues());
    return indexedKeyValues;
  }
}
