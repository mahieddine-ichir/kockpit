package org.kockpit.audit;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.kockpit.audit.api.AuditReportWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@RequiredArgsConstructor
class AuditReportsQueueHandler {

    private final List<AuditReportNotificationService> auditReportNotificationServices;

    // buffer size in number of records
    private final int bufferSize;

    private final int bufferFreeThreshold;

    // batch size in bytes
    private final int maxBatchSize;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private LinkedBlockingQueue<AuditReportWrapper> auditReportsBlockingQueue;

    @PostConstruct
    void init() {
        this.auditReportsBlockingQueue = new LinkedBlockingQueue<>(bufferSize);
    }

    @PreDestroy
    void shutdown() {
        executor.shutdown();
    }

    private void notify(List<AuditReportWrapper> partition) {
        auditReportNotificationServices.forEach(auditReportNotificationService -> auditReportNotificationService.notify(partition));
    }

    void processSingle(AuditReportWrapper auditReportWrapper) {
        this.notify(List.of(auditReportWrapper));
    }

    boolean add(AuditReportWrapper auditReportWrapper) {
        try {
            if (auditReportsBlockingQueue.remainingCapacity() <= 0) {
                log.warn("Buffer full, triggering poll and notify (blocking)");
                this.pollAndNotify();  // blocking to ensure space is freed
            }
            auditReportsBlockingQueue.add(auditReportWrapper);
            if (auditReportsBlockingQueue.size() >= bufferFreeThreshold) {
                log.warn("Buffer threshold reached -> free buffer (buffer size {})", auditReportsBlockingQueue.size());
                this.pollAndNotifyAsync();
            }
            return true;
        } catch (Exception e) {
            log.warn("Cannot add audit report to buffer, limit exceeded ! {}", auditReportsBlockingQueue.size(), e);
            this.pollAndNotifyAsync();
            return false;
        }
    }

    private void pollAndNotifyAsync() {
        executor.submit(this::pollAndNotify);
    }

    void pollAndNotify() {
        if (auditReportsBlockingQueue.isEmpty()) {
            return;
        }
        log.trace("Polling buffer of size: {}, batchSizeInBytes: {}", auditReportsBlockingQueue.size(), maxBatchSize);
        long currentTimeMillis = System.currentTimeMillis();

        List<AuditReportWrapper> polledRecords = new ArrayList<>();
        int totalSize = 0;
        while (true) {
            AuditReportWrapper peeked = auditReportsBlockingQueue.peek();
            if (peeked == null) {
                break;
            }
            // Stop if adding this record would exceed batch size (but always take at least one)
            // !polledRecords.isEmpty() ensures that we add at least an element to the list
            if (!polledRecords.isEmpty() && totalSize + peeked.data().length > maxBatchSize) {
                break;
            }
            AuditReportWrapper record = auditReportsBlockingQueue.poll();
            polledRecords.add(record);
            totalSize += record.data().length;
        }
        if (polledRecords.isEmpty()) {
            return;
        }
        if (polledRecords.size() == 1 && totalSize > maxBatchSize) {
            log.error("Record size {} is larger than authorized limit {} => skip record!", totalSize, maxBatchSize);
            return;
        }

        log.trace("-> Polled {} records ({} bytes) in {} ms", polledRecords.size(), totalSize, System.currentTimeMillis() - currentTimeMillis);
        this.notify(polledRecords);
    }
}
