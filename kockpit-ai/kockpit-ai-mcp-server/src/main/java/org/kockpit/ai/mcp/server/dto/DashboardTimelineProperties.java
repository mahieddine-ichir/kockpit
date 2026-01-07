package org.kockpit.ai.mcp.server.dto;

import lombok.Data;

@Data
public class DashboardTimelineProperties {

  private String label;

  private String[] properties;

  private String renderer;
}
