package org.kockpit.audit.notification.eventhub;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.kockpit.audit.api.AuditReportWrapper;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Slf4j
record EventHubAuditReportNotificationService(
        EventHubProducerClient producerClient
) implements AuditReportNotificationService {

    @Override
    public void notify(List<AuditReportWrapper> auditReports) {
        this.publishEvents(auditReports);
    }

    void publishEvents(List<AuditReportWrapper> auditReports) {
        if (CollectionUtils.isEmpty(auditReports)) {
            return;
        }
        // A batch carries a single partition key: one batch (or more, when full) per domain/env/appId,
        // so the reports of an application keep their order on one partition.
        Map<String, List<AuditReportWrapper>> reportsByKey = auditReports.stream()
                .collect(groupingBy(EventHubAuditReportNotificationService::partitionKey, LinkedHashMap::new, toList()));
        reportsByKey.forEach(this::publish);
    }

    private void publish(String partitionKey, List<AuditReportWrapper> auditReports) {
        try {
            CreateBatchOptions options = new CreateBatchOptions().setPartitionKey(partitionKey);
            EventDataBatch batch = producerClient.createBatch(options);
            for (AuditReportWrapper report : auditReports) {
                EventData event = new EventData(report.data());
                if (batch.tryAdd(event)) {
                    continue;
                }
                send(batch);
                batch = producerClient.createBatch(options);
                if (!batch.tryAdd(event)) {
                    log.error("Audit report {} ({} bytes) exceeds the Event Hub max batch size {}, dropped",
                            report.id(), report.data().length, batch.getMaxSizeInBytes());
                }
            }
            send(batch);
        } catch (Exception e) {
            log.error("Error sending records to event hub {}", producerClient.getEventHubName(), e);
        }
    }

    private void send(EventDataBatch batch) {
        if (batch.getCount() > 0) {
            producerClient.send(batch);
        }
    }

    // Same key as the Kafka notifier: consumers reading the hub through its Kafka endpoint get it as record key.
    static String partitionKey(AuditReportWrapper report) {
        return "%s-%s-%s".formatted(report.domain(), report.env(), report.appId());
    }
}
