package org.kockpit.audit.module.web;

import org.kockpit.audit.api.IndexedKeyValue;
import lombok.Data;

import java.util.List;

@Data
public class WebAuditReportData {
  public static final String TYPE = "builtin.web";
  private List<IndexedKeyValue> indexedKeyValues;
  private WebAuditEvent webAuditEvent;

  public WebAuditReportData(WebAuditEvent webAuditEvent, List<IndexedKeyValue> indexedKeyValues) {
    this.webAuditEvent = webAuditEvent;
    this.indexedKeyValues = indexedKeyValues;
  }

  public void addIndexedKeyValue(IndexedKeyValue indexedKeyValue) {
    this.indexedKeyValues.add(indexedKeyValue);
  }
}
