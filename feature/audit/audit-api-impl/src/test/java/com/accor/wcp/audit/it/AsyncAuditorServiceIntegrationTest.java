package com.accor.wcp.audit.it;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.accor.wcp.audit.AuditReport.AuditJsonReport;
import com.accor.wcp.audit.AuditorService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest()
@ActiveProfiles({"obfuscation1"})
class AsyncAuditorServiceIntegrationTest {

  @Autowired private AuditorService auditorService;

  @Autowired private LocalNotificationService localNotificationService;

  @Test
  void should_audit_async() throws InterruptedException {
    auditorService.startAudit();

    auditorService.stopAuditAndNotify();

    // Wait for scheduler to process
    Thread.sleep(2000);

    List<AuditJsonReport> auditReport = localNotificationService.getAuditReport();
    assertThat(auditReport).isNotNull();
  }
}
