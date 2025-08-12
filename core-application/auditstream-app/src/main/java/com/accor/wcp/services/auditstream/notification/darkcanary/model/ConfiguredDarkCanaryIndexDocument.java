package com.accor.wcp.services.auditstream.notification.darkcanary.model;

import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryConfiguration;

public record ConfiguredDarkCanaryIndexDocument(
        DarkCanaryIndexDocument darkCanaryIndexDocument,
        DarkCanaryConfiguration darkCanaryConfiguration
) {
}
