package com.example.insightsstreamingappapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class InsightStats {
    private long totalRequests;
    private float successRate;
    private float errorRate;
    private float avgDuration;
    private Map<String, Long> countsPerDomainEnv;
    private Map<String, Long> statusDistribution;
    private Map<String, Long> methodDistribution;
    private Map<String, Float> avgDurationByStatus;
}
