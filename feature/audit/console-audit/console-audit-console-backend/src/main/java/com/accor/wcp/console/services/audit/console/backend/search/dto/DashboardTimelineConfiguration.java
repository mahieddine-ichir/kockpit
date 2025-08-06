package com.accor.wcp.console.services.audit.console.backend.search.dto;

import lombok.Data;

import java.util.Collection;

@Data
public class DashboardTimelineConfiguration {

  private Collection<DashboardTimelineProperties> displayedProperties;

  private DashboardExecutionTime executionLag;
}
