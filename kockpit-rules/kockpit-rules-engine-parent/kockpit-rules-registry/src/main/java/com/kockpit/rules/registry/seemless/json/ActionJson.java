package com.kockpit.rules.registry.seemless.json;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder()
@AllArgsConstructor
public class ActionJson {
  private static int incrementalId;
  private String id;
  private String name;
  private String description;
  private DetailsJson details;
  private String spel;

  public ActionJson() {
    id = "actionjson_" + incrementalId++;
  }
}
