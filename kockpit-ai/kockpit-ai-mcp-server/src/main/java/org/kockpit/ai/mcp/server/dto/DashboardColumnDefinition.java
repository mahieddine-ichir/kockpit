package org.kockpit.ai.mcp.server.dto;

import lombok.Data;

@Data
public class DashboardColumnDefinition {

  private String label;

  private String name;

  private String renderer;
}
