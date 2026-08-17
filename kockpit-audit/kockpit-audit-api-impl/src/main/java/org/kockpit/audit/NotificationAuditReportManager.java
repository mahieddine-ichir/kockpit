package org.kockpit.audit;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReport;
import org.kockpit.audit.api.AuditReportWrapper;
import org.kockpit.audit.api.CompressionService;
import org.springframework.scheduling.annotation.Scheduled;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class NotificationAuditReportManager {

  private final AuditReportsQueueHandler auditReportsQueueHandler;
  private final CompressionService compressionService;

  private final boolean asyncEnableFlag;
  private final boolean compressionEnabled;

  // java.time est integre a jackson-databind 3. On force l'ecriture des Instant en
  // timestamps numeriques (defaut Jackson 2, defaut inverse en Jackson 3) : c'est le
  // format du flux audit, lu par les starters kafka/kinesis et indexe dans OpenSearch.
  private final ObjectMapper objectMapper = JsonMapper.builder()
          .enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
          // Jackson 3 trie les proprietes alphabetiquement par defaut ; on conserve l'ordre
          // de declaration (defaut Jackson 2) pour ne pas changer le JSON produit.
          .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
          .build();

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
    } catch (JacksonException e) {
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
