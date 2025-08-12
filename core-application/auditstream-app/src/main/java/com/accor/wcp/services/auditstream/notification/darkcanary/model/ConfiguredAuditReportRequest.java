package com.accor.wcp.services.auditstream.notification.darkcanary.model;

import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryConfiguration;

public record ConfiguredAuditReportRequest(
        AuditReportRequest auditReportRequest,
        DarkCanaryConfiguration darkCanaryConfiguration
) {
}
