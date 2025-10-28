package org.kockpit.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.kockpit.audit.api.AuditReport;
import org.kockpit.audit.api.AuditReportNotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
class AuditReportsQueueHandler {

    private final AuditPostProcessor auditPostProcessor;

    private final List<AuditReportNotificationService> auditReportNotificationServices;

    private final int partitionSize;

    private final LinkedBlockingQueue<AuditReport> auditReportsBlockingQueue;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .addMixIn(AuditReport.class, AuditReportJacksonConfigMixIn.class)
            .registerModule(new JavaTimeModule());

    private final int bufferFreeThreshold;

    private final Executor executor = Executors.newFixedThreadPool(1);

    public AuditReportsQueueHandler(AuditPostProcessor auditPostProcessor,
                                    List<AuditReportNotificationService> auditReportNotificationServices, int partitionSize,
                                    int bufferSize, int bufferFreeThreshold
    ) {
        this.auditPostProcessor = auditPostProcessor;
        this.auditReportNotificationServices = auditReportNotificationServices;
        this.partitionSize = partitionSize;
        this.auditReportsBlockingQueue = new LinkedBlockingQueue<>(bufferSize);
        this.bufferFreeThreshold = bufferFreeThreshold;
    }

    void doProcess() {
        executor.execute(this::doProcessBlocking);
    }

    void doProcessBlocking() {
        if (auditReportsBlockingQueue.isEmpty()) {
            return;
        }
        log.trace("Process buffer of size: {}", auditReportsBlockingQueue.size());
        long currentTimeMillis = System.currentTimeMillis();
        List<AuditReport> auditReports = new ArrayList<>();
        auditReportsBlockingQueue.drainTo(auditReports);

        log.trace("Processed buffer of size: {} in {} ms", auditReportsBlockingQueue.size(), System.currentTimeMillis() - currentTimeMillis);
        this.processAuditsReports(auditReports);
    }

    private void processAuditsReports(List<AuditReport> auditReports) {
        if (CollectionUtils.isEmpty(auditReports)) {
            return;
        }
        List<AuditReport.AuditJsonReport> auditJsonReports =
                auditReports.stream()
                        .map(auditPostProcessor::process)
                        .map(this::mapToAuditJsonReport)
                        .filter(Objects::nonNull)
                        .toList();

        ListUtils.partition(auditJsonReports, partitionSize).forEach(partition ->
                auditReportNotificationServices.forEach(auditReportNotificationService -> auditReportNotificationService.notify(partition)));
    }

    public void processSingle(AuditReport auditReport) {
        AuditReport process = auditPostProcessor.process(auditReport);
        if (process == null) {
            log.warn("AuditReport {} is null upon processing", auditReport);
            return;
        }
        auditReportNotificationServices.forEach(auditReportNotificationService -> auditReportNotificationService.notify(List.of(mapToAuditJsonReport(process))));
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

    boolean add(AuditReport auditReport, boolean blocking) {
        try {
            if (auditReportsBlockingQueue.remainingCapacity() <= 0) {
                this.doProcess();
            }
            if (blocking) {
                auditReportsBlockingQueue.put(auditReport);
            } else {
                auditReportsBlockingQueue.add(auditReport);
            }
            if (auditReportsBlockingQueue.remainingCapacity() <= bufferFreeThreshold) {
                log.warn("Buffer capacity threshold exceeded -> free buffer (buffer size {})", auditReportsBlockingQueue.size());
                this.doProcess();
            }
            return true;
        } catch (Exception e) {
            log.warn("Cannot add audit report to buffer, limit exceeded ! {}", auditReportsBlockingQueue.size(), e);
            this.doProcess();
            return false;
        }
    }
}
