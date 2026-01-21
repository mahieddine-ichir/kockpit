package org.kockpit.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReport;
import org.kockpit.audit.api.AuditReportWrapper;
import org.kockpit.audit.api.CompressionService;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class NotificationAuditReportManager {

  private final AuditReportsQueueHandler auditReportsQueueHandler;
  private final CompressionService compressionService;

  private final boolean asyncEnableFlag;
  private final boolean compressionEnabled;

  private final ObjectMapper objectMapper = new ObjectMapper()
          .registerModule(new JavaTimeModule());

  @SneakyThrows
  public void addAuditReport(AuditReport auditReport) {
    // Direct mode
    if (!asyncEnableFlag) {
      auditReportsQueueHandler.processSingle(wrap(auditReport));
      return;
    }

    if (! this.auditReportsQueueHandler.add(wrap(auditReport))) {
      log.error("Error adding audit report to queue -> increase buffer capacity!");
    }
  }

  AuditReportWrapper wrap(AuditReport auditReport) {
    return AuditReportWrapper.of(toBytes(auditReport), auditReport);
  }

  private byte[] toBytes(AuditReport auditReport) {
    try {
      byte[] json = objectMapper.writeValueAsBytes(auditReport);
      if (compressionEnabled) {
        return compressionService.compress(json);
      } else {
        return json;
      }
    } catch (IOException e) {
      log.error("Error serializing Audit object {} to json. Error: {}", auditReport, e.getMessage(), e);
      return null;
    }
  }

  @PreDestroy
  public void flushAuditReportsBlockingQueue() {
    notifyBufferedAudit();
  }

  @Scheduled(
      fixedDelayString = "${kockpit.audit.notification.async-delay-ms:10000}",
      timeUnit = TimeUnit.MILLISECONDS)
  public void notifyBufferedAudit() {
    log.trace("(scheduler) Notify buffered audit reports");
    this.auditReportsQueueHandler.pollAndNotify();
  }
}
