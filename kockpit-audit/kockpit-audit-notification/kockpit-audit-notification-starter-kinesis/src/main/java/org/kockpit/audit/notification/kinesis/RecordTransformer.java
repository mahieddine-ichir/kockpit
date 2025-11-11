package org.kockpit.audit.notification.kinesis;

import org.kockpit.audit.api.AuditReport;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;

import java.util.function.Function;

public interface RecordTransformer extends Function<AuditReport.AuditJsonReport, PutRecordsRequestEntry> {
}
