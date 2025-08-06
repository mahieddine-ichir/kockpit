package com.accor.wcp.obfuscation.masker;

/**
 * Masker definition is responsible for masking input stream with the correct masker.
 */
public interface MaskerService {

  /**
   * Mask input data with found masker.
   * @param data string to mask
   * @param maskerId masker reference
   * @return masked string
   */
  String mask(String data, String maskerId);
}
