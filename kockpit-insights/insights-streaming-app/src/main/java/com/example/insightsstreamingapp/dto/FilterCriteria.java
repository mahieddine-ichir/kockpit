package com.example.insightsstreamingapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterCriteria {
    private String domain;
    private String env;
    private String method;
    private String status;
    private Instant from;
    private Instant to;
}