package com.accor.kengine.registry.seamless.json;

import com.accor.kengine.DocumentationDetails;
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
