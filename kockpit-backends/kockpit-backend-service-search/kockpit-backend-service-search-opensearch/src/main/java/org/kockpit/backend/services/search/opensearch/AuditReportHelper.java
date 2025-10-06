package org.kockpit.backend.services.search.opensearch;

public class AuditReportHelper {

    public static String getAuditAliasName(String domain, String indexName, String env) {
        return domain + "-" + indexName + "-" + env + "-read".toLowerCase();
    }
}
