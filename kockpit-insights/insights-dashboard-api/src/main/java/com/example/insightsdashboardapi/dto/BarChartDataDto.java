package com.example.insightsdashboardapi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BarChartDataDto {
    private String category;
    private Double value;
}
