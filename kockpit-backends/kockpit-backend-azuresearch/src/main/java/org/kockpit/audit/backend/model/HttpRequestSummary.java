package org.kockpit.audit.backend.model;

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

