package org.kockpit.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.kockpit.audit.api.AuditReport;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NotificationAuditReportManager {
  private final boolean asyncEnableFlag;
  private final int partitionSize;
  private final boolean blockIfFullBuffer;
  private final LinkedBlockingQueue<AuditReport> auditReportsBlockingQueue;
  private final List<AuditReportNotificationService> auditReportNotificationServices;
  private final AuditPostProcessor auditPostProcessor;
  private final ObjectMapper objectMapper;

  public NotificationAuditReportManager(
      boolean asyncEnableFlag,
      List<AuditReportNotificationService> auditReportNotificationServices,
      int bufferSize,
      int partitionSize,
      boolean blockIfFullBuffer,
      AuditPostProcessor auditPostProcessor) {
    this.asyncEnableFlag = asyncEnableFlag;
    this.auditReportNotificationServices = auditReportNotificationServices;
    this.partitionSize = partitionSize;
    this.blockIfFullBuffer = blockIfFullBuffer;
    this.auditReportsBlockingQueue = new LinkedBlockingQueue<>(bufferSize);
    this.auditPostProcessor = auditPostProcessor;

    this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
  }

  @SneakyThrows
  public void addAuditReport(AuditReport auditReport) {
    // Direct mode
    if (!asyncEnableFlag) {
      doProcess(List.of(auditReport));
      return;
    }

    // Async mode
    if (blockIfFullBuffer) {
      auditReportsBlockingQueue.put(auditReport);
    } else {
      auditReportsBlockingQueue.add(auditReport);
    }
  }

  @PreDestroy
  public void flushAuditReportsBlockingQueue() {
    notifyBufferedAudit();
  }

  @Scheduled(
      fixedDelayString = "${kockpit.sdk.service.audit.notification.async.delay_ms:10000}",
      timeUnit = TimeUnit.MILLISECONDS)
  public void notifyBufferedAudit() {
    log.trace("(scheduler) Notify buffered audit reports");
    if (CollectionUtils.isEmpty(auditReportNotificationServices)) {
      return;
    }

    List<AuditReport> auditReports = new ArrayList<>();
    auditReportsBlockingQueue.drainTo(auditReports);

    if (auditReports.isEmpty()) {
      return;
    }

    doProcess(auditReports);
  }

  private void doProcess(List<AuditReport> auditReports) {
    List<AuditReport.AuditJsonReport> auditJsonReports =
            auditReports.stream()
                    .map(auditPostProcessor::process)
                    .map(this::mapToAuditJsonReport)
                    .filter(Objects::nonNull)
                    .toList();

    ListUtils.partition(auditJsonReports, partitionSize)
        .forEach(sublist ->
                auditReportNotificationServices.forEach(service -> service.notify(sublist)));
  }

  private AuditReport.AuditJsonReport mapToAuditJsonReport(AuditReport auditReport) {
    try {
      String json = objectMapper.writeValueAsString(auditReport);
      return new InternalAuditJsonReport(auditReport, json);
    } catch (JsonProcessingException e) {
      log.error("Error serializing Audit object {} to json. Error: {}", auditReport, e.getMessage(), e);
      return null;
    }
  }

  @Getter
  @Deprecated
  private static class InternalAuditJsonReport extends AuditReport.AuditJsonReport {
    private final String auditJson;

    public InternalAuditJsonReport(AuditReport auditReport, String auditJson) {
      super(auditReport, new ArrayList<>());
      this.auditJson = auditJson;
    }
  }
}
