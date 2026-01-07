package org.kockpit.ai.mcp.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditReportDocument {

  private String domain;

  private String env;

  private String appId;

  private String version;

  private String artifact;

  private String requestId;

  private Instant start;

  private Instant end;

  private String hostname;

  private List<Map<String, Object>> indexedExtensions;

  private String originalJsonValue;

  private String compressedOriginalJsonValue;

  private Integer ttl;
}
