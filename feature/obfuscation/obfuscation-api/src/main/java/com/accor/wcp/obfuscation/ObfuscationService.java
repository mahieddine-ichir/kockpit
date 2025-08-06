package com.accor.wcp.obfuscation;

/**
 * Obfuscation service definition.
 * It is the entrypoint to obfuscate any data with a given configuration.
 **/
public interface ObfuscationService {

  /**
   * Obfuscate given string and return the result.
   * @param data original string
   * @param config obfuscation configuration to apply
   * @return obfuscated data result
   */
  String obfuscate(String data, ObfuscateConfig config);

  /**
   * Obfuscate an object with several properties.
   * @param data POJO you want to obfuscate
   * @param config object obfuscation configuration
   * @return obfuscated object (same reference as input one)
   */
  <T> T obfuscateObject(T data, ObjectObfuscateConfig config);

}
