package com.accor.wcp.console.services.audit.console.backend.search.dto;

import lombok.Data;

@Data
public class DashboardTimelineProperties {

  private String label;

  private String[] properties;

  private String renderer;
}
