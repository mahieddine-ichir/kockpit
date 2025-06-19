package org.kockpit.audit.backoffice.DataModel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Data
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
@JsonDeserialize(builder = AuditReport.AuditReportBuilder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditReport {

    private final String id;
    private final String domain;
    private final String env;
    private final String requestId;
    private String appId;
    private final String hostname;
    private final String version;
    private final String artifact;
    private final Instant start;
    private Instant end;
    private Integer ttl;


    @Builder.Default
    private final List<IndexedKeyValue> indexedKeyValues = new ArrayList<>();
    @Builder.Default
    private final List<AuditIndexedKeyValuesComputeFunction> indexedKeyValuesComputeFunctions =
            new ArrayList<>();
    @JsonDeserialize(using = AuditMapDeserializer.class)
    @Builder.Default
    private List<Audit> audits = new ArrayList<>();

    @Data
    @Deprecated
    public abstract static class AuditJsonReport {
        private final AuditReport auditReport;
        @Deprecated private final List<Function<String, String>> auditReportPostProcess;

        public abstract String getAuditJson();
    }
//    @JsonIgnore
//    public Map<String, Audit> getAuditMapView() {
//        return audits;
//    }

    // TODO clean that => must separate audit accumulator and audit report message (final data)
//    @JsonIgnore
//    public Map<String, Audit> auditsMap() {
//        return audits;
//    }

    public List<Audit> getAudits() {
        return new ArrayList<>(audits.subList(0, audits.size() - 1));
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class AuditReportBuilder {
    }

}
