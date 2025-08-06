package com.accor.wcp.console.services.audit.console.backend.search.dto;

import java.io.Serializable;
import java.util.Collection;
import lombok.Data;

@Data
public class AuditViewDto implements Serializable {

  private String domain;

  private String env;

  private String name;

  private String label;

  private String route;

  // list of corresponding appId in ES
  private Collection<String> appIds;

  private Collection<ColumnDefinition> resultColumns;

  private Collection<AuditMetadataDto> searchMetadatas;

  private Collection<DashboardColumnDefinition> dashboardColumns;

  private DashboardTimelineConfiguration dashboardTimelineConfiguration;
}
