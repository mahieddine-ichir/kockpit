package com.example.insightsstreamingappapi;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class InsightDocument {
    private String id;
    private Instant timestamp;
    private InsightStats  stats;
}
