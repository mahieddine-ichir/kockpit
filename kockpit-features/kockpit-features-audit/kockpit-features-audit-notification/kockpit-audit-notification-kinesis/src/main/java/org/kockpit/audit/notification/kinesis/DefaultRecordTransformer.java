package org.kockpit.audit.notification.kinesis;

import lombok.RequiredArgsConstructor;
import org.kockpit.audit.api.AuditReportWrapper;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;

@RequiredArgsConstructor
public class DefaultRecordTransformer implements RecordTransformer {

    private final RecordPartitioner recordPartitioner;

    @Override
    public PutRecordsRequestEntry apply(AuditReportWrapper reportWrapper) {
        String partitionKey = recordPartitioner.computePartitionKey(reportWrapper);
        return PutRecordsRequestEntry.builder()
                .partitionKey(partitionKey)
                .data(SdkBytes.fromByteArray(reportWrapper.data()))
                .build();
    }
}
