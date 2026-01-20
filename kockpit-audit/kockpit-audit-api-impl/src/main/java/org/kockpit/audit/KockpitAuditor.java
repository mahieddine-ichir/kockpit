package org.kockpit.audit;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditKeyConstants;
import org.kockpit.audit.api.AuditReport;
import org.kockpit.audit.api.AuditorService;
import org.kockpit.audit.api.IndexedKeyValue;
import org.slf4j.MDC;
import org.springframework.boot.info.BuildProperties;
import org.springframework.util.CollectionUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.allNotNull;
import static org.kockpit.audit.KockpitAuditorEvent.closeCurrentAuditEvents;
import static org.kockpit.audit.KockpitAuditorKeyValue.closeCurrentAuditIndexedKeyValues;

@Slf4j
class KockpitAuditor implements AuditorService {

  private final String domain;
  private final String env;
  private final String appId;
  private final BuildProperties buildProperties;
  private final NotificationAuditReportManager notificationAuditReportManager;
  private final String hostname;
  private final String artifact;
  private final String version;
  private final Integer defaultTtl;

  KockpitAuditor(
      String domain,
      String env,
      String appId,
      BuildProperties buildProperties,
      NotificationAuditReportManager notificationAuditReportManager,
      Integer defaultTtl) {
    this.domain = domain;
    this.env = env;
    this.appId = appId;
    this.buildProperties = buildProperties;
    this.notificationAuditReportManager = notificationAuditReportManager;
    this.hostname = retrieveHostName();
    this.artifact = retrieveArtifact();
    this.version = retrieveVersion();
    this.defaultTtl = defaultTtl;
  }

  public void startAudit() {
    this.startAuditInternal();
  }

  private void startAuditInternal() {
    AuditReportContainer.resetReport();
    String auditId = UUID.randomUUID().toString();
    AuditReportContainer.setAuditReport(
        AuditReport.builder()
            .id(auditId)
            .requestId(auditId)
            .start(Instant.now())
            .domain(this.domain)
            .env(this.env)
            .appId(this.appId)
            .hostname(this.hostname)
            .artifact(this.artifact)
            .version(this.version)
            .ttl(this.defaultTtl)
            .build());
    MDC.put("auditId", auditId);
  }

  @Override
  public boolean isAuditStarted() {
    return AuditReportContainer.isAuditStarted();
  }

  private String retrieveVersion() {
    return nonNull(buildProperties) ? buildProperties.getVersion() : "";
  }

  private String retrieveArtifact() {
    return nonNull(buildProperties) ? buildProperties.getArtifact() : "";
  }

  public void stopAuditAndNotify() {
    AuditReport auditReport = this.stopAudit();
    this.notify(auditReport);
  }

  public AuditReport stopAudit() {
    // Late computations (Audit Report
    closeCurrentAuditEvents();
    closeCurrentAuditIndexedKeyValues();

    AuditReport auditReport = AuditReportContainer.getAuditReport();
    computeTtl(auditReport);
    computeAppId(auditReport);

    auditReport.setEnd(Instant.now());
    setAuditReportDurationKeyValue(auditReport);
    AuditReportContainer.resetReport();
    return auditReport;
  }

  private void computeAppId(AuditReport auditReport) {
    // Defined in audit key value
    auditReport.getIndexedKeyValues().stream()
        .filter(kv -> kv.getKey().equals(AuditKeyConstants.AUDIT_APPID))
        .findFirst()
        .map(IndexedKeyValue::getValue)
        .ifPresent(auditReport::setAppId);
  }

  private void computeTtl(AuditReport auditReport) {
    // Defined in audit report directly?
    if (isNull(auditReport.getTtl())) {
      auditReport.setTtl(defaultTtl);
    }

    // Defined in audit key value
    auditReport.getIndexedKeyValues().stream()
        .filter(kv -> kv.getKey().equals(AuditKeyConstants.AUDIT_TTL))
        .findFirst()
        .map(IndexedKeyValue::getValueInteger)
        .ifPresent(auditReport::setTtl);
  }

  public void notify(AuditReport auditReport) {
    notificationAuditReportManager.addAuditReport(auditReport);
  }

  private String retrieveHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException ex) {
      return UNDEFINED;
    }
  }

  private static void setAuditReportDurationKeyValue(AuditReport auditReport) {
    Instant start = auditReport.getStart();
    Instant end = auditReport.getEnd();
    Long duration = allNotNull(start, end) ? end.toEpochMilli() - start.toEpochMilli() : null;

    if (!CollectionUtils.isEmpty(auditReport.getIndexedKeyValues())) {
      auditReport.getIndexedKeyValues().add(IndexedKeyValue.of("duration", duration));
    }
  }

  private static final String UNDEFINED = "UNDEFINED";
}
