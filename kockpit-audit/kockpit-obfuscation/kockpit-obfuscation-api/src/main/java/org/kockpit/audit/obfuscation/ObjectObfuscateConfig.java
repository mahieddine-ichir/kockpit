package org.kockpit.audit.obfuscation;

import java.util.Map;

/**
 * Obfuscation Configuration for an object.
 */
public interface ObjectObfuscateConfig extends ObfuscateConfig {

  /**
   * @return obfuscation configuration for each property (of object) you want to obfuscate.
   */
  Map<String, ObfuscateConfig> getObfuscateConfigByProperty();
}
