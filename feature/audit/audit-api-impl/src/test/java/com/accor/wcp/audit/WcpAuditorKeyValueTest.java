package com.accor.wcp.audit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class WcpAuditorKeyValueTest {

  private WcpAuditorKeyValue underTest = new WcpAuditorKeyValue();

  @Test
  void addIndexedKeyValues() {
    AuditReport auditReport = AuditReport.builder().build();
    AuditReportContainer.setAuditReport(auditReport);
    underTest.addIndexedKeyValues(List.of(IndexedKeyValue.of("Test", "value")));
    assertThat(auditReport.getIndexedKeyValues().size()).isEqualTo(1);
  }

  @Test
  void setTtl() {
    AuditReport auditReport = AuditReport.builder().build();
    AuditReportContainer.setAuditReport(auditReport);
    underTest.setTtl(10);
    assertThat(auditReport.getTtl()).isEqualTo(10);
  }
}
