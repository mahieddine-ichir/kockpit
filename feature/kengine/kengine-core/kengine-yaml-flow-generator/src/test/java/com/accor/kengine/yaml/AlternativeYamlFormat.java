package com.accor.kengine.yaml;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AlternativeYamlFormat {
  private String flow;
  private String description;
  private String rule;
  private List<String> predicates;
  private List<String> actions;
  private List<String> callbacks;
}
