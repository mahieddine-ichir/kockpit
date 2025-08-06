package com.accor.wcp.console.services.sqsdlq.model;

import lombok.Data;

@Data
public class SqsDlqColumnDefinitionDto {

  private String label;

  private String name;

  private Integer limitDisplay;
}
