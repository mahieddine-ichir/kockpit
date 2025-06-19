package org.kockpit.audit.backoffice.DTOs;

import java.time.Instant;

public record HttpRequestSummary(
        String method,
        String uri,
        int status,
        long duration,
        Instant timestamp,
        String traceId
) {

}

