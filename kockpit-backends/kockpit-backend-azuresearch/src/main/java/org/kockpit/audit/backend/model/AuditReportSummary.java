package org.kockpit.audit.backend.model;

import java.time.Instant;

public record AuditReportSummary(
        String id,
        String domain,
        String env,
        Instant start,
        String appId,
        String requestId,
        Integer ttl,
        Integer status
) {
}

