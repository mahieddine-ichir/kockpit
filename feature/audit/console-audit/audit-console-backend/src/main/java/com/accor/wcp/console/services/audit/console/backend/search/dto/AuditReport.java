package com.accor.wcp.console.services.audit.console.backend.search.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditReport {

  private String domain;

  private String env;

  private String appId;

  private String version;

  private String artifact;

  private String requestId;

  private Instant start;

  private Instant end;

  private String hostname;

  private Integer ttl;

  @Deprecated private List<Map<String, Object>> extensions;

  private List<Map<String, Object>> audits;

  private List<Map<String, Object>> indexedKeyValues;
}
