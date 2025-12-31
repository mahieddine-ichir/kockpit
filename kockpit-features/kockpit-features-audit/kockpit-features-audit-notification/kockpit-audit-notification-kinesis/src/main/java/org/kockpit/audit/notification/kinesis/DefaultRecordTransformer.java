package org.kockpit.audit.notification.kinesis;

import lombok.RequiredArgsConstructor;
import org.kockpit.audit.api.AuditReport;
import org.kockpit.audit.api.CompressionService;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;

@RequiredArgsConstructor
public class DefaultRecordTransformer implements RecordTransformer {

    private final RecordPartitioner recordPartitioner;
    private final CompressionService compressionService;

    @Override
    public PutRecordsRequestEntry apply(AuditReport.AuditJsonReport auditJsonReport) {
        String partitionKey = recordPartitioner.computePartitionKey(auditJsonReport);
        byte[] compressedData = compressionService.compress(auditJsonReport.getAuditJson());
        return PutRecordsRequestEntry.builder()
                .partitionKey(partitionKey)
                .data(SdkBytes.fromByteArray(compressedData))
                .build();
    }
}
