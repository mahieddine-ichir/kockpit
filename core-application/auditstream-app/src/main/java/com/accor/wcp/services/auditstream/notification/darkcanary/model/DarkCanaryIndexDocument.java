package com.accor.wcp.services.auditstream.notification.darkcanary.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DarkCanaryIndexDocument {

    private List<KeyValuePair> id;

    @JsonProperty("@timestamp")
    private Long timestamp;

    private String domain;

    private String appId;

    private String version;

    private String artifact;

    private String hostname;

    private String requestId;

    private String responseLeft;

    private String responseRight;

    private String env;

    private List<PropertyDifference> differences;

    private List<Endpoint> endpoints = new ArrayList<>();

    @Deprecated
    private Long diffDuration;

    private DiffDuration durations;

    private String errorMessage;

    private String traceId;

    @JsonIgnore
    private boolean aggregated;

    // internal Opensearch ID
    @JsonIgnore
    private String _id;
}
