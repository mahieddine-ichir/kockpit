package org.kockpit.audit.backend.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
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

    //private List<SearchIndexedKeyValue> indexedKeyValues;

    private List<SearchAudit> audits;
}
