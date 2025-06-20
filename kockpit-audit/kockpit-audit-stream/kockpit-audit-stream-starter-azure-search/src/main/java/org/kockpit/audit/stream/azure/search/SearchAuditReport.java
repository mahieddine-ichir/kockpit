package org.kockpit.audit.stream.azure.search;

import com.azure.search.documents.indexes.SearchableField;
import com.azure.search.documents.indexes.SimpleField;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchAuditReport {

    @SearchableField(isKey = true)
    private String id;

    @SimpleField(isSortable = true, isFilterable = true)
    private String domain;

    @SimpleField(isSortable = true, isFilterable = true)
    private String env;

    @SimpleField
    private String requestId;

    @SimpleField(isSortable = true, isFilterable = true)
    private String appId;

    @SimpleField(isSortable = true, isFilterable = true)
    private String hostname;

    @SimpleField(isSortable = true, isFilterable = true)
    private String version;

    @SearchableField(isSortable = true, isFilterable = true)
    private String artifact;

    @SimpleField(isSortable = true)
    private Long start;

    @SimpleField(isSortable = true)
    private Long end;

    @SimpleField(isSortable = true, isFilterable = true)
    private Integer ttl;

    @SimpleField(isFilterable = true)
    private List<SearchIndexedKeyValue> indexedKeyValues;

    @SimpleField(isSortable = true, isFilterable = true)
    private List<SearchAudit> audits;
}
