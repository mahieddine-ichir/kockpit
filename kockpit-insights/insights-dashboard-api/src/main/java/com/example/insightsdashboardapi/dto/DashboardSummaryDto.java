package com.example.insightsdashboardapi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardSummaryDto {
    private Long totalRequests;
    private Double averageSuccessRate;
    private Double averageErrorRate;
    private Double averageDuration;
}
