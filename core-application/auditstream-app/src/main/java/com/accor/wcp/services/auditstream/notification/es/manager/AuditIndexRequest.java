package com.accor.wcp.services.auditstream.notification.es.manager;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditIndexRequest {
  private String docId;
  private String domain;
  private String env;
  private int ttl;
  private String auditReportJson;
}
