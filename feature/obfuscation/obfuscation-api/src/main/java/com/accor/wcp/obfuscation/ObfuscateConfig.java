package com.accor.wcp.obfuscation;

/**
 * Obfuscation Configuration marker.
 * Each kind of obfuscation has it own configuration which must extend this interface.
 */
public interface ObfuscateConfig {

  String DEFAULT_MASKER_ID = "DEFAULT";

  /**
   * Which masker to apply by default?
   * @return masker id to use
   */
  default String getMaskerId() {
    return DEFAULT_MASKER_ID;
  }
}
