package com.accor.wcp.audit.module.web;

import com.accor.wcp.audit.IndexedKeyValue;
import java.util.List;
import lombok.Data;

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
