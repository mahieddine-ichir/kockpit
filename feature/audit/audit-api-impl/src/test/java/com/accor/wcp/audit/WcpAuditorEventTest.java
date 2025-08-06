package com.accor.wcp.audit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WcpAuditorEventTest {

  private WcpAuditorEvent underTest;

  @BeforeEach
  void init() {
    AuditReportContainer.setAuditReport(AuditReport.builder().build());
    underTest = new WcpAuditorEvent();
  }

  @AfterEach
  void clean() {
    AuditReportContainer.resetReport();
  }

  @Test
  void addAuditEvents() {
    // Given
    AbstractAuditEvent event1 = AbstractAuditEvent.builder().build();
    AbstractAuditEvent event2 = AbstractAuditEvent.builder().build();
    AbstractAuditEvent event3 = AbstractAuditEvent.builder().build();

    // When
    underTest.addAuditEvents("type1", Arrays.asList(event1, event2, event3));
    underTest.addAuditEvents("type2", Arrays.asList(event1, event2, event3));
    underTest.addAuditEvents("type1", Arrays.asList(event1));
    // This does not affect report because of read only list is returned
    AuditReportContainer.getAuditReport().getAudits().get(0).getEvents().add(event1);

    // Then
    assertThat(AuditReportContainer.getAuditReport().getAuditsMap()).hasSize(2);
    assertThat(AuditReportContainer.getAuditReport().getAudits()).hasSize(2);
    assertThat(AuditReportContainer.getAuditReport().getAudits().get(0).getEvents()).hasSize(3);
    assertThat(AuditReportContainer.getAuditReport().getAudits().get(1).getEvents()).hasSize(4);
  }
}
