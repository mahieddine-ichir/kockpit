package com.accor.wcp.obfuscation.impl.obfuscators.json;

import com.accor.wcp.obfuscation.ObfuscateConfig;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class JsonObfuscateConfig implements ObfuscateConfig {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PathConfig {

    private String path;
    private String maskerId;
  }

  private List<PathConfig> pathConfigs;
}
