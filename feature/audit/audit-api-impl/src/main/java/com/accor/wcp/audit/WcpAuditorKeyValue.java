package com.accor.wcp.audit;

import java.util.List;

public class WcpAuditorKeyValue implements AuditorKeyValueService, AuditorTtlService {

  @Override
  public void addIndexedKeyValues(List<IndexedKeyValue> indexedKeyValues) {
    AuditReportContainer.getAuditReport().getIndexedKeyValues().addAll(indexedKeyValues);
  }

  @Override
  public void addIndexedKeyValues(AuditIndexedKeyValuesComputeFunction computeFunction) {
    AuditReportContainer.getAuditReport()
        .getIndexedKeyValuesComputeFunctions()
        .add(computeFunction);
  }

  @Override
  public void setTtl(int ttl) {
    AuditReportContainer.getAuditReport().setTtl(ttl);
  }

  static void closeCurrentAuditIndexedKeyValues() {
    // Late compute of indexed key values
    AuditReport auditReport = AuditReportContainer.getAuditReport();
    auditReport
        .getIndexedKeyValuesComputeFunctions()
        .forEach(f -> auditReport.getIndexedKeyValues().addAll(f.compute()));
    auditReport.getIndexedKeyValuesComputeFunctions().clear();
  }
}
