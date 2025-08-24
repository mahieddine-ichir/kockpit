package com.example.insightsstreamingapp.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TimeSeriesDataDto {
    private Instant timestamp;
    private Double value;
    private String metric;
}
