package org.kockpit.audit.stream.s3;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.kockpit.audit.stream.api.model.AuditReport;

/**
 * Groups {@link AuditReport}s by domain/env/appId before writing to S3, so each S3 object holds
 * reports from a single (domain, env, appId) combination.
 */
@Getter
@Builder
@EqualsAndHashCode
@ToString
class S3AuditGroup {

    private String domain;
    private String env;
    private String appId;

    static S3AuditGroup of(AuditReport auditReport) {
        return S3AuditGroup.builder()
                .domain(auditReport.getDomain())
                .env(auditReport.getEnv())
                .appId(auditReport.getAppId())
                .build();
    }

    String keyPrefix() {
        return "d=%s/e=%s/a=%s".formatted(domain, env, appId);
    }
}
