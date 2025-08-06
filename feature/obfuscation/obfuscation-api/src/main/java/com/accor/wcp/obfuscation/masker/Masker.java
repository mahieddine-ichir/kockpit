package com.accor.wcp.obfuscation.masker;

/**
 * Definition of a masker.
 * It is responsible for replacing incoming data with an internal mask (pattern) to hide some data.
 */
public interface Masker {

  /**
   * @return masker unique identifier
   */
  String getType();

  /**
   * Mask given data in a custom way.
   * @param input data to mask
   * @return masked data
   */
  String mask(String input);
}
