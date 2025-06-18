package org.kockpit.audit.api;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.util.Collections.synchronizedList;

@Builder
@Data
@Setter
public class AuditReport {
  // fixme @Builder.Default private final AuditReportVersion auditReportVersion = V2_6;
  private final String id;
  private final String domain;
  private final String env;
  private final String requestId;
  private String appId;
  private final String hostname;
  private final String version;
  private final String artifact;
  private final Instant start;
  private Instant end;
  private Integer ttl;
  private final List<IndexedKeyValue> indexedKeyValues = synchronizedList(new ArrayList<>());
  private final List<AuditIndexedKeyValuesComputeFunction> indexedKeyValuesComputeFunctions =
      synchronizedList(new ArrayList<>());
  private final Map<String, Audit> audits = new ConcurrentHashMap<>();

  @Data
  @Deprecated
  public abstract static class AuditJsonReport {
    private final AuditReport auditReport;
    @Deprecated private final List<Function<String, String>> auditReportPostProcess;

    public abstract String getAuditJson();
  }

  public Map<String, Audit> getAuditsMap() {
    return audits;
  }

  public List<Audit> getAudits() {
    return new ArrayList<>(audits.values());
  }
}
