package com.example.insightsstreamingapp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BarChartDataDto {
    private String category;
    private Double value;
}
