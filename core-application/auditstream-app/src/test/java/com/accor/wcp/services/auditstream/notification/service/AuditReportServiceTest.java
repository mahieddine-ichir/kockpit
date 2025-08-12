package com.accor.wcp.services.auditstream.notification.service;

import static org.junit.jupiter.api.Assertions.*;

import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import org.junit.jupiter.api.Test;

class AuditReportServiceTest {

  @Test
  void computeTtl() {
    AuditReportRequest dto = new AuditReportRequest();
    dto.setTtl(1);
    assertEquals(1, AuditReportService.computeTtl(dto));
    dto.setTtl(2);
    assertEquals(3, AuditReportService.computeTtl(dto));
    dto.setTtl(3);
    assertEquals(3, AuditReportService.computeTtl(dto));
    dto.setTtl(4);
    assertEquals(5, AuditReportService.computeTtl(dto));
    dto.setTtl(5);
    assertEquals(5, AuditReportService.computeTtl(dto));
    dto.setTtl(6);
    assertEquals(10, AuditReportService.computeTtl(dto));
    dto.setTtl(10);
    assertEquals(10, AuditReportService.computeTtl(dto));
    dto.setTtl(11);
    assertEquals(15, AuditReportService.computeTtl(dto));
    dto.setTtl(15);
    assertEquals(15, AuditReportService.computeTtl(dto));
    dto.setTtl(16);
    assertEquals(30, AuditReportService.computeTtl(dto));
    dto.setTtl(30);
    assertEquals(30, AuditReportService.computeTtl(dto));
    dto.setTtl(31);
    assertEquals(60, AuditReportService.computeTtl(dto));
    dto.setTtl(60);
    assertEquals(60, AuditReportService.computeTtl(dto));
    dto.setTtl(70);
    assertEquals(60, AuditReportService.computeTtl(dto));
  }
}
