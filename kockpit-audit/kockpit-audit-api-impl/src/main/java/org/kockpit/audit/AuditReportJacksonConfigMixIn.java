package org.kockpit.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.kockpit.audit.api.Audit;

import java.util.Map;

@JsonIgnoreProperties("indexedKeyValuesComputeFunctions")
abstract class AuditReportJacksonConfigMixIn {

    @JsonIgnore
    abstract Map<String, Audit> getAuditsMap();
}
