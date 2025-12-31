package org.kockpit.audit.module.web;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.kockpit.audit.api.IndexedKeyValue;

import java.util.List;

@Data
@RequiredArgsConstructor
public class WebAuditReportData {

  public static final String TYPE = "builtin.web";

  private final WebAuditEvent webAuditEvent;
  private final List<IndexedKeyValue> indexedKeyValues;
}
