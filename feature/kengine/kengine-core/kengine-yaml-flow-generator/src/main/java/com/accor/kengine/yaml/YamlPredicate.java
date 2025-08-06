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
public class YamlPredicate {
  private List<String> names;
  private YamlRule ok;
  private YamlRule ko;
  private YamlRule lastly;
}
