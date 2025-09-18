package org.kockpit.audit;

import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReport;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NotificationAuditReportManager {
  private final boolean asyncEnableFlag;
  private final boolean blockIfFullBuffer;
  private final boolean silentErrorProcessing;
  private final AuditReportsQueueHandler auditReportsQueueHandler;

  public NotificationAuditReportManager(
      List<AuditReportNotificationService> auditReportNotificationServices,
      AuditPostProcessor auditPostProcessor,
      boolean asyncEnableFlag,
      int bufferSize, int bufferThreshold, int partitionSize, boolean blockIfFullBuffer, boolean silentErrorProcessing) {
    this.asyncEnableFlag = asyncEnableFlag;
    this.blockIfFullBuffer = blockIfFullBuffer;
    this.silentErrorProcessing = silentErrorProcessing;
    this.auditReportsQueueHandler = new AuditReportsQueueHandler(auditPostProcessor, auditReportNotificationServices, partitionSize, bufferSize, bufferThreshold);
  }

  @SneakyThrows
  public void addAuditReport(AuditReport auditReport) {
    // Direct mode
    if (!asyncEnableFlag) {
      auditReportsQueueHandler.processSingle(auditReport);
      return;
    }

    if (! this.auditReportsQueueHandler.add(auditReport, blockIfFullBuffer)) {
      if (!silentErrorProcessing) {
        log.error("Error processing/adding Audit report {}", auditReport);
      }
    }
  }

  @PreDestroy
  public void flushAuditReportsBlockingQueue() {
    notifyBufferedAudit();
  }

  @Scheduled(
      fixedDelayString = "${kockpit.sdk.service.audit.notification.async-delay-ms:10000}",
      timeUnit = TimeUnit.MILLISECONDS)
  public void notifyBufferedAudit() {
    log.info("(scheduler) Notify buffered audit reports");
    this.auditReportsQueueHandler.doProcessBlocking();
  }
}
