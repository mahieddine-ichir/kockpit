// TODO
// package com.accor.wcp.audit;
//
// import static com.accor.wcp.audit.AuditReportContainer.AUDIT_REPORT_HOLDER;
// import static java.lang.Thread.sleep;
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.when;
//
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.boot.info.BuildProperties;
//
// @ExtendWith(MockitoExtension.class)
// class WcpAuditorTest {
//
//  @Mock BuildProperties buildProperties;
//
//  @Mock AuditReportNotificationService auditReportNotificationService;
//
//  @Mock ObfuscateAuditService obfuscateAuditService;
//
//  @Test
//  void shouldBuiltAnAuditResultWithCommonInfos() throws InterruptedException {
//    when(buildProperties.getVersion()).thenReturn("A_VERSION");
//    when(buildProperties.getArtifact()).thenReturn("AN_ARTIFACT");
//    WcpAuditor auditer =
//        new WcpAuditor(
//            "wcp",
//            "local",
//            "Test",
//            buildProperties,
//            auditReportNotificationService,
//            obfuscateAuditService);
//    auditer.startAudit();
//    sleep(1000);
//    assertThat(auditer.getAuditReport().getId()).isNotNull();
//    assertThat(auditer.getAuditReport().getStart()).isNotNull();
//    assertThat(auditer.getAuditReport().getAppId()).isEqualTo("Test");
//    assertThat(auditer.getAuditReport().getDomain()).isEqualTo("wcp");
//    assertThat(auditer.getAuditReport().getEnv()).isEqualTo("local");
//    assertThat(auditer.getAuditReport().getHostname()).isNotNull();
//    assertThat(auditer.getAuditReport().getVersion()).isEqualTo("A_VERSION");
//    assertThat(auditer.getAuditReport().getArtifact()).isEqualTo("AN_ARTIFACT");
//    auditer.stopAuditAndNotify();
//    assertThatExceptionOfType(AuditNotStartedException.class)
//        .isThrownBy(auditer::getAuditReport)
//        .withMessage("Audit not started, report not initalized");
//  }
//
//  @Test
//  void shouldBuiltAnAuditResultWithCommonInfos_ExceptArtifactAndVersion()
//      throws InterruptedException {
//    WcpAuditor auditer =
//        new WcpAuditor(
//            "wcp", "local", "Test", null, auditReportNotificationService, obfuscateAuditService);
//    auditer.startAudit();
//    sleep(1000);
//    assertThat(auditer.getAuditReport().getAppId()).isEqualTo("Test");
//    assertThat(auditer.getAuditReport().getDomain()).isEqualTo("wcp");
//    assertThat(auditer.getAuditReport().getEnv()).isEqualTo("local");
//    assertThat(auditer.getAuditReport().getVersion()).isEmpty();
//    assertThat(auditer.getAuditReport().getArtifact()).isEmpty();
//    auditer.stopAuditAndNotify();
//    assertThatExceptionOfType(AuditNotStartedException.class)
//        .isThrownBy(auditer::getAuditReport)
//        .withMessage("Audit not started, report not initalized");
//  }
//
//  @Test
//  void shouldBuiltAnAuditResultWithError() throws InterruptedException {
//    WcpAuditor auditer =
//        new WcpAuditor(
//            "wcp", "local", "Test", null, auditReportNotificationService, obfuscateAuditService);
//    auditer.startAudit();
//    sleep(1000);
//    auditer.stopAuditAndNotify();
//    assertThatExceptionOfType(AuditNotStartedException.class)
//        .isThrownBy(auditer::getAuditReport)
//        .withMessage("Audit not started, report not initalized");
//  }
//
//  @Test
//  void shouldThrow_AuditNotStartedException() {
//    AuditReportContainer.resetReport();
//    WcpAuditor auditer =
//        new WcpAuditor(
//            "wcp", "local", "Test", null, auditReportNotificationService, obfuscateAuditService);
//    assertThatExceptionOfType(AuditNotStartedException.class)
//        .isThrownBy(auditer::getAuditReport)
//        .withMessage("Audit not started, report not initalized");
//  }
//
//  @Test
//  void shouldReturnEmptyList() {
//    WcpAuditor auditer =
//        new WcpAuditor(
//            "wcp", "local", "Test", null, auditReportNotificationService, obfuscateAuditService);
//    auditer.startAudit();
//    assertThat(auditer.getAuditReport().getAudits()).isEmpty();
//    auditer.stopAuditAndNotify();
//    assertThatExceptionOfType(AuditNotStartedException.class)
//        .isThrownBy(auditer::getAuditReport)
//        .withMessage("Audit not started, report not initalized");
//  }
//
//  @Test
//  void shouldReturnAListOfModuleReport() {
//    WcpAuditor auditer =
//        new WcpAuditor(
//            "wcp", "local", "Test", null, auditReportNotificationService, obfuscateAuditService);
//    auditer.startAudit();
//    Audit audit = mock(Audit.class);
//
//    // Can not add audit by this way!
//    auditer.getAuditReport().getAudits().add(audit);
//    assertThat(auditer.getAuditReport().getAudits()).isEmpty();
//
//    auditer.stopAuditAndNotify();
//    assertThatExceptionOfType(AuditNotStartedException.class)
//        .isThrownBy(auditer::getAuditReport)
//        .withMessage("Audit not started, report not initalized");
//  }
//
//  @Nested
//  class AuditTest {
//    @Test
//    void isAuditStarted_nominal_when_audit_is_not_started() {
//      // GIVEN
//      WcpAuditor auditor =
//          new WcpAuditor(
//              "wcp",
//              "local",
//              "Test",
//              buildProperties,
//              auditReportNotificationService,
//              obfuscateAuditService);
//      AUDIT_REPORT_HOLDER.set(null);
//
//      // WHEN
//      boolean isAuditStarted = auditor.isAuditStarted();
//
//      // THEN
//      assertThat(isAuditStarted).isFalse();
//    }
//
//    @Test
//    void isAuditStarted_nominal_when_audit_is_started() {
//      // GIVEN
//      WcpAuditor auditor =
//          new WcpAuditor(
//              "wcp",
//              "local",
//              "Test",
//              buildProperties,
//              auditReportNotificationService,
//              obfuscateAuditService);
//      AUDIT_REPORT_HOLDER.set(mock(AuditReport.class));
//
//      // WHEN
//      boolean isAuditStarted = auditor.isAuditStarted();
//
//      // THEN
//      assertThat(isAuditStarted).isTrue();
//    }
//  }
//
//  @Test
//  void should_compute_ttl() {
//    // Given
//    when(buildProperties.getVersion()).thenReturn("A_VERSION");
//    when(buildProperties.getArtifact()).thenReturn("AN_ARTIFACT");
//    WcpAuditor auditer =
//        new WcpAuditor(
//            "wcp",
//            "local",
//            "Test",
//            buildProperties,
//            auditReportNotificationService,
//            5,
//            obfuscateAuditService);
//
//    // default case
//    auditer.startAudit();
//    AuditReport auditReport = auditer.stopAudit();
//    assertThat(auditReport.getTtl()).isEqualTo(5);
//
//    // force case
//    auditer.startAudit();
//    auditer.getAuditReport().setTtl(100);
//    auditReport = auditer.stopAudit();
//    assertThat(auditReport.getTtl()).isEqualTo(100);
//
//    // through internal key value
//    auditer.startAudit();
//    auditer
//        .getAuditReport()
//        .getIndexedKeyValues()
//        .add(IndexedKeyValue.of(AuditKeyConstants.AUDIT_TTL, 50));
//    auditReport = auditer.stopAudit();
//    assertThat(auditReport.getTtl()).isEqualTo(50);
//  }
// }
