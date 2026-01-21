package org.kockpit.audit.notification.kinesis;

import org.kockpit.audit.api.AuditReportWrapper;

public interface RecordPartitioner {

    String computePartitionKey(AuditReportWrapper auditReport);
}
