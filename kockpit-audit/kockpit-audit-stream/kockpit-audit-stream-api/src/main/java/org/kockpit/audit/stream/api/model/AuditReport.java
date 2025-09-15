package org.kockpit.audit.stream.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditReport {
    private String id;
    private String domain;
    private String env;
    private String requestId;
    private String appId;
    private String hostname;
    private String version;
    private String artifact;
    private Instant start;
    private Instant end;
    private Integer ttl;
    private List<IndexedKeyValue> indexedKeyValues;
    private List<Audit> audits;
}
