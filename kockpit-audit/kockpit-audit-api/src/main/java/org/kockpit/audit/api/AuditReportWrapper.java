package org.kockpit.audit.api;

public record AuditReportWrapper (
        String domain,
        String env,
        String appId,
        String artifactId,
        String id,
        byte[] data
) {

    public static AuditReportWrapper of(byte[] data, AuditReport auditReport) {
        return new AuditReportWrapper(
                auditReport.getDomain(),
                auditReport.getEnv(),
                auditReport.getAppId(),
                auditReport.getArtifact(),
                auditReport.getId(),
                data
        );
    }
}
