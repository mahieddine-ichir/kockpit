package org.kockpit.audit.notification.kinesis;

import org.kockpit.audit.api.AuditReport;

public interface RecordPartitioner {

    String computePartitionKey(AuditReport.AuditJsonReport auditReport);
}
