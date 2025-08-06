package com.accor.wcp.console.services.audit.console.backend.search.dashboard;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
public class DashboardSearchResultDto {

    private String index;

    private String id;

    private String domain;

    private String env;

    private String appId;

    private Instant start;

    private Instant end;

    private String requestId;

    private List<Map<String, Object>> indexedKeyValues;
}
