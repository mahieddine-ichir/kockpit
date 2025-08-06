package com.accor.kengine.registry.seamless.json;

import static java.util.Objects.nonNull;

import java.util.List;
import lombok.Data;

@Data
public class FlowJson {
  private static int incrementalId;
  private String id;
  private String name;
  private DetailsJson details;
  private List<RuleJson> referentials;

  public FlowJson() {
    id = "flowjson_" + incrementalId++;
  }

  public String getId() {
    return nonNull(name) ? name : id;
  }
}
