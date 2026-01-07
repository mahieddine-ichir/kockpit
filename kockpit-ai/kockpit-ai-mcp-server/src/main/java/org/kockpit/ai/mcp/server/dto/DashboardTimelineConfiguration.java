package org.kockpit.ai.mcp.server.dto;

import lombok.Data;

import java.util.Collection;

@Data
public class DashboardTimelineConfiguration {

  private Collection<DashboardTimelineProperties> displayedProperties;

  private DashboardExecutionTime executionLag;
}
