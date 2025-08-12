package com.accor.wcp.services.auditstream.notification.darkcanary.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DarkCanaryIndexDocumentByDifference {

    @JsonProperty("@timestamp")
    private Long timestamp;

    private String domain;

    private String env;

    private String appId;

    private String version;

    private String artifact;

    private String hostname;

    private String requestId;

    private PropertyDifference difference;

    private Endpoint endpoint;

    private String traceId;
}
