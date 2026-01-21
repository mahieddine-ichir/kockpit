package org.kockpit.audit.notification.kinesis;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.kockpit.audit.api.AuditReportWrapper;

@RequiredArgsConstructor
public class DefaultRecordPartitioner implements RecordPartitioner {

    private final int partitionKeyMaxLength;

    @Override
    public String computePartitionKey(AuditReportWrapper auditReport) {
        String partitionKey =
                String.format(
                        "d=%s,e=%s,a=%s,ar=%s,id=%s",
                        auditReport.domain(),
                        auditReport.env(),
                        auditReport.appId(),
                        auditReport.artifactId(),
                        auditReport.id());
        return StringUtils.truncate(partitionKey, partitionKeyMaxLength);
    }
}
