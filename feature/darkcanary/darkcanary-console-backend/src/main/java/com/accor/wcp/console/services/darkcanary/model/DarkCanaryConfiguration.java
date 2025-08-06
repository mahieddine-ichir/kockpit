package com.accor.wcp.console.services.darkcanary.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DarkCanaryConfiguration {

    private String name;

    private String label;

    private String appId;

    private String domain;

    // canary testing source : audit or remote
    private CanaryTestingSource testingSource = CanaryTestingSource.audit;

    // headers to use on remote call, when testingSource = remote
    private List<DarkCanaryHeader> headers;

    private List<DarkCanaryIdConfiguration> id;

    private List<DarkCanaryEndpoint> endpoints;

    private List<DarkCanaryWatch> excludes;

    private List<DarkCanaryWatch> includes;

    private Boolean strict;

    /**
     * [0.0, 1.0): manages to rate at which to apply the record & replay
     */
    private double callRate;

    private TimeInterval[] allowedTimeIntervals;

    private Integer ttl;
}
