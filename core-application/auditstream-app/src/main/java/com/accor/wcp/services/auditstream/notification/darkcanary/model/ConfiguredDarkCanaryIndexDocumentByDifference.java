package com.accor.wcp.services.auditstream.notification.darkcanary.model;

import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryConfiguration;

public record ConfiguredDarkCanaryIndexDocumentByDifference(
        DarkCanaryIndexDocumentByDifference darkCanaryIndexDocumentByDifference,
        DarkCanaryConfiguration darkCanaryConfiguration
) {
}
