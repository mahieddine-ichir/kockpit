package com.accor.wcp.services.auditstream.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditReportRequest implements Serializable {

  private String auditReportVersion;

  private String domain;

  private String env;

  private String appId;

  private String version;

  private String artifact;

  private String requestId;

  private Instant start;

  private Instant end;

  private String hostname;

  @Deprecated private List<Map<String, Object>> extensions;

  private List<Map<String, Object>> audits;
  private List<Map<String, Object>> indexedKeyValues;

  private Integer ttl;
}
