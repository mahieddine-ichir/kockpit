package org.kockpit.audit.stream.opensearch.model;

import lombok.Data;

import java.util.List;

@Data
public class SearchAuditReport {

    private String id;

    private String domain;

    private String env;

    private String requestId;

    private String appId;

    private String hostname;

    private String version;

    private String artifact;

    private Long start;

    private Long end;

    private Integer ttl;

    private Long timestamp;

    private List<SearchIndexedKeyValue> indexedKeyValues;

    private List<SearchAudit> audits;
}
