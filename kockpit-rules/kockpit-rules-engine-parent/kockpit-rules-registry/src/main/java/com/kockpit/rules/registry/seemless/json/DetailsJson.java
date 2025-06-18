package com.kockpit.rules.registry.seemless.json;

import com.kockpit.rules.DocumentationDetails;
import lombok.Data;

@Data
public class DetailsJson implements DocumentationDetails {
  private String name;
  private String description;

  @Override
  public String getCode() {
    return name;
  }

  @Override
  public String getDocumentation() {
    return description;
  }
}
