package com.accor.wcp.console.services.audit.console.backend.search.dto;

import lombok.Data;

@Data
public class ColumnDefinition {

  private String label;

  private String name;

  private String renderer;

  private Boolean dashboard;
}
