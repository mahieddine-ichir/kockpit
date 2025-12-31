package org.kockpit.audit.notification.kinesis;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.kockpit.audit.api.AuditReport;

@RequiredArgsConstructor
public class DefaultRecordPartitioner implements RecordPartitioner {

    private final int partitionKeyMaxLength;

    @Override
    public String computePartitionKey(AuditReport.AuditJsonReport auditReport) {
        String partitionKey =
                String.format(
                        "d=%s,e=%s,a=%s,ar=%s,id=%s",
                        auditReport.getAuditReport().getDomain(),
                        auditReport.getAuditReport().getEnv(),
                        auditReport.getAuditReport().getAppId(),
                        auditReport.getAuditReport().getArtifact(),
                        auditReport.getAuditReport().getRequestId());
        return StringUtils.truncate(partitionKey, partitionKeyMaxLength);
    }
}
