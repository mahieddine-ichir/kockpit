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
public class YamlNode {
  private String name;
  private List<YamlNode> ok;
  private List<YamlNode> ko;
  private List<YamlNode> lastly;
}
