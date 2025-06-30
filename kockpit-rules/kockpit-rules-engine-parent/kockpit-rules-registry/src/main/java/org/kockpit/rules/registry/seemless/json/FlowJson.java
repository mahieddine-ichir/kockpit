package org.kockpit.rules.registry.seemless.json;

import lombok.Data;

import java.util.List;

import static java.util.Objects.nonNull;

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
