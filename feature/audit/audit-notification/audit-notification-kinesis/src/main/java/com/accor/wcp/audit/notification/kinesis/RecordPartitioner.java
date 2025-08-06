package com.accor.wcp.audit.notification.kinesis;

import com.accor.wcp.audit.AuditReport;
import org.apache.commons.lang3.StringUtils;

public class RecordPartitioner {

    private static final int PARTITION_KEY_MAX_LENGTH = 256;

    String computePartitionKey(AuditReport.AuditJsonReport auditReport) {
        String partitionKey =
                String.format(
                        "d=%s,e=%s,a=%s,ar=%s,id=%s",
                        auditReport.getAuditReport().getDomain(),
                        auditReport.getAuditReport().getEnv(),
                        auditReport.getAuditReport().getAppId(),
                        auditReport.getAuditReport().getArtifact(),
                        auditReport.getAuditReport().getRequestId());
        return StringUtils.truncate(partitionKey, PARTITION_KEY_MAX_LENGTH);
    }
}
