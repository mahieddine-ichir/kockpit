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
public class YamlRule {
  private String name;
  private String details;
  private List<String> actions;
  private YamlPredicate predicate;
}
