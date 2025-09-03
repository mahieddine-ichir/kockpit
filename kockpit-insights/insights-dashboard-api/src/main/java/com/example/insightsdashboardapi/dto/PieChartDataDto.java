package com.example.insightsdashboardapi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PieChartDataDto {
    private String label;
    private Long value;
}
