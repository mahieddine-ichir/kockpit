package com.accor.wcp.services.auditstream.notification;

import java.util.List;
import java.util.function.Consumer;

/**
 * Interface for {@link AuditReportRequest}'s consumption.
 */
public interface AuditReportRequestConsumer extends Consumer<List<AuditReportRequest>> {
}
