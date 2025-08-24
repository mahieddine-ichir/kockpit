package com.example.insightsstreamingapp.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class RealtimeMetricsDto {
    private Long totalRequests;
    private Double successRate;
    private Double errorRate;
    private Double avgDuration;
    private Instant timestamp;
}
