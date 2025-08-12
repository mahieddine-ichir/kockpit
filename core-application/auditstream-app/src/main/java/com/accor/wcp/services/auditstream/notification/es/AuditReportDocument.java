package com.accor.wcp.services.auditstream.notification.es;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditReportDocument {

  @JsonProperty("@timestamp")
  private Long timestamp;

  private String domain;

  private String env;

  private String appId;

  private String version;

  private String artifact;

  private String requestId;

  private Instant start;

  private Instant end;

  private String hostname;

  // String -> 'indexedKeyValues', Object -> List com.accor.wcp.audit.indexedKeyValue.class
  private List<Map<String, Object>> indexedExtensions;

  private String compressedOriginalJsonValue;
}
