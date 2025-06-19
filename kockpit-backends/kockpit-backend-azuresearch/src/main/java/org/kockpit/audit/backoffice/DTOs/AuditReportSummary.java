package org.kockpit.audit.backoffice.DTOs;

import java.time.Instant;

public record AuditReportSummary(
        String id,
        String domain,
        String env,
        Instant start,
        String appId,
        String requestId,
        Integer ttl) {

}

