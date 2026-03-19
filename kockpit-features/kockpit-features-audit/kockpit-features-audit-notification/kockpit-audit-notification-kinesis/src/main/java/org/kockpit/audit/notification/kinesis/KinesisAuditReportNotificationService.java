package org.kockpit.audit.notification.kinesis;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.kockpit.audit.api.AuditReportWrapper;
import org.springframework.util.CollectionUtils;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResponse;

import java.util.List;
import java.util.Objects;

@Slf4j
record KinesisAuditReportNotificationService(
        KinesisAsyncClient kinesisAsyncClient,
        String streamName,
        RecordTransformer recordTransformer
) implements AuditReportNotificationService {

    @Override
    public void notify(List<AuditReportWrapper> auditReports) {
        this.publishEvents(auditReports);
    }

    void publishEvents(List<AuditReportWrapper> auditReports) {
        if (CollectionUtils.isEmpty(auditReports)) {
            return;
        }
        List<PutRecordsRequestEntry> putRecordsRequestEntries = auditReports.stream()
                .map(recordTransformer)
                .filter(Objects::nonNull)
                .toList();

        if (CollectionUtils.isEmpty(putRecordsRequestEntries)) {
            return;
        }
        try {
            PutRecordsRequest.Builder requestBuilder = PutRecordsRequest.builder().records(putRecordsRequestEntries);
            if (streamName.startsWith("arn:")) {
                requestBuilder.streamARN(streamName);
            } else {
                requestBuilder.streamName(streamName);
            }
            PutRecordsRequest putRecordsRequest = requestBuilder.build();
            PutRecordsResponse putRecordsResponse = kinesisAsyncClient.putRecords(putRecordsRequest).get();
            if (putRecordsResponse.failedRecordCount() > 0) {
                putRecordsResponse.records().stream().filter(record -> Objects.nonNull(record.errorCode()))
                        .forEach(record -> log.error("Error sending record {}, ({} - {})", record, record.errorCode(), record.errorMessage()));
            }
        } catch (Exception e) {
            log.error("Error sending records to kinesis audit", e);
        }
    }
}
