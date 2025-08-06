package com.accor.kengine.registry.seamless.json;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class PredicateJson {
  private static int incrementalId;
  private String id;
  private String name;
  private String description;
  private DetailsJson details;
  private String spel;

  public PredicateJson() {
    id = "predicatejson_" + incrementalId++;
  }
}
