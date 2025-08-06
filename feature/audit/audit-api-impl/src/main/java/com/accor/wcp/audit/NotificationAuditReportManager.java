package com.accor.wcp.audit;

import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

@Slf4j
class NotificationAuditReportManager {
  private final boolean asyncEnableFlag;
  private final boolean blockIfFullBuffer;
  private final boolean silentErrorProcessing;
  private final AuditReportsQueueHandler auditReportsQueueHandler;

  public NotificationAuditReportManager(
      boolean asyncEnableFlag,
      AuditReportNotificationService auditReportNotificationService,
      int bufferSize, int bufferThreshold,
      int partitionSize,
      boolean blockIfFullBuffer,
      boolean silentErrorProcessing,
      AuditPostProcessor auditPostProcessor) {
    this.asyncEnableFlag = asyncEnableFlag;
    this.blockIfFullBuffer = blockIfFullBuffer;
    this.silentErrorProcessing = silentErrorProcessing;
    this.auditReportsQueueHandler = new AuditReportsQueueHandler(auditPostProcessor,
            auditReportNotificationService,
            partitionSize, bufferSize, bufferThreshold);
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
      fixedDelayString = "${wcp.sdk.service.audit.notification.async.delay-ms:1000}",
      timeUnit = TimeUnit.MILLISECONDS)
  public void notifyBufferedAudit() {
    this.auditReportsQueueHandler.doProcess();
  }
}
