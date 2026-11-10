package org.kockpit.audit.notification.kinesis;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.kockpit.audit.api.AuditReportWrapper;
import org.springframework.util.CollectionUtils;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResultEntry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
record KinesisAuditReportNotificationService(
        KinesisAsyncClient kinesisAsyncClient,
        String streamName,
        RecordTransformer recordTransformer,
        int maxRetries,
        Duration retryBackoff
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
        List<PutRecordsRequestEntry> pending = putRecordsRequestEntries;
        Map<String, Long> lastErrors = Map.of();
        try {
            for (int retry = 0; retry <= maxRetries; retry++) {
                if (retry > 0) {
                    backOff(retry);
                }
                PutRecordsResponse response = kinesisAsyncClient.putRecords(request(pending)).get();
                // A throttled record doesn't fail the request: it is only reported in the response
                if (response.failedRecordCount() == null || response.failedRecordCount() == 0) {
                    return;
                }
                lastErrors = errorCounts(response);
                pending = failedEntries(pending, response);
            }
            log.error("Kinesis audit putRecords rejected {} audit reports after {} retries - dropped. Errors: {}",
                    pending.size(), maxRetries, lastErrors);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while sending {} audit reports to kinesis", pending.size(), e);
        } catch (Exception e) {
            log.error("Error sending records to kinesis audit", e);
        }
    }

    private PutRecordsRequest request(List<PutRecordsRequestEntry> entries) {
        PutRecordsRequest.Builder requestBuilder = PutRecordsRequest.builder().records(entries);
        if (streamName.startsWith("arn:")) {
            requestBuilder.streamARN(streamName);
        } else {
            requestBuilder.streamName(streamName);
        }
        return requestBuilder.build();
    }

    private void backOff(int retry) throws InterruptedException {
        long delay = retryBackoff.toMillis() << (retry - 1);
        Thread.sleep(delay / 2 + ThreadLocalRandom.current().nextLong(delay / 2 + 1));
    }

    // Response records are in the same order as the request entries
    private static List<PutRecordsRequestEntry> failedEntries(List<PutRecordsRequestEntry> entries, PutRecordsResponse response) {
        List<PutRecordsRequestEntry> failed = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            if (response.records().get(i).errorCode() != null) {
                failed.add(entries.get(i));
            }
        }
        return failed;
    }

    private static Map<String, Long> errorCounts(PutRecordsResponse response) {
        return response.records().stream()
                .filter(result -> result.errorCode() != null)
                .collect(Collectors.groupingBy(PutRecordsResultEntry::errorCode, Collectors.counting()));
    }
}
