package org.kockpit.audit.obfuscation.impl.obfuscators.xml;

import org.kockpit.audit.obfuscation.ObfuscateConfig;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class XmlObfuscateConfig implements ObfuscateConfig {

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
