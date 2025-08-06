package com.accor.wcp.audit.notification.kinesis;

import com.accor.wcp.audit.AuditReport;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class GZipRecordCompressor implements Function<AuditReport.AuditJsonReport, PutRecordsRequestEntry> {

    private final RecordPartitioner recordPartitioner = new RecordPartitioner();

    @Override
    public PutRecordsRequestEntry apply(AuditReport.AuditJsonReport auditReport) {
        try {
            byte[] recordBytes = compressRecord(auditReport.getAuditJson());
            String partitionKey = recordPartitioner.computePartitionKey(auditReport);

            return PutRecordsRequestEntry.builder()
                    .partitionKey(partitionKey)
                    .data(SdkBytes.fromByteArray(recordBytes))
                    .build();

        } catch (IOException e) {
            log.error(
                    "Error compressRecord object {}. Error: {}",
                    auditReport.getAuditJson(),
                    e.getMessage(),
                    e);
            return null;
        }
    }

    private byte[] compressRecord(String content) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(content.getBytes(StandardCharsets.UTF_8));
        gzip.close();

        return out.toByteArray();
    }
}
