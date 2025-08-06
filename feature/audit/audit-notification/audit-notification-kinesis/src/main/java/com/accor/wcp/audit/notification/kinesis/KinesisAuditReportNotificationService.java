package com.accor.wcp.audit.notification.kinesis;

import com.accor.wcp.audit.AuditReport.AuditJsonReport;
import com.accor.wcp.audit.AuditReportNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;

import java.util.List;
import java.util.Objects;

@Slf4j
public class KinesisAuditReportNotificationService implements AuditReportNotificationService {

  private final KinesisAsyncClient kinesisClient;
  private final String streamName;
  private final GZipRecordCompressor gzipRecordCompressor;

  public KinesisAuditReportNotificationService(
      KinesisAsyncClient kinesisClient, String streamName) {
    this.kinesisClient = kinesisClient;
    this.streamName = streamName;
    this.gzipRecordCompressor = new GZipRecordCompressor();
  }

  @Override
  public void notify(List<AuditJsonReport> auditReports) {

    List<PutRecordsRequestEntry> putRecordsRequestEntries =
        auditReports.stream()
            .map(gzipRecordCompressor)
            .filter(Objects::nonNull)
            .toList();

    if (CollectionUtils.isEmpty(putRecordsRequestEntries)) {
      return;
    }

    PutRecordsRequest putRecordsRequest =
        PutRecordsRequest.builder()
            .streamName(streamName)
            .records(putRecordsRequestEntries)
            .build();

    try {
      kinesisClient.putRecords(putRecordsRequest).get();
    } catch (Exception e) {
      log.error("Error sending records to kinesis audit. Error: {}", e.getMessage(), e);
    }
  }
}
