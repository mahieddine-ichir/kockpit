package com.accor.wcp.obfuscation;

/**
 * Obfuscate handler definition.
 * @param <T> associated {@link ObfuscateConfig} class.
 */
public interface Obfuscate<T extends ObfuscateConfig> {

  /**
   * Default method to cast config with <T> generic type
   * @param data data to obfuscate
   * @param config configuration to use
   * @return obfuscated data
   */
  @SuppressWarnings("unchecked")
  default String obfuscate(String data, ObfuscateConfig config) {
    return this.doObfuscate(data, (T) config);
  }

  /**
   * Real method to implement.
   * @see #obfuscate(String, ObfuscateConfig)
   */
  String doObfuscate(String data, T config);
}
