package com.accor.wcp.obfuscation.impl.masker.maskers;

import com.accor.wcp.obfuscation.masker.Masker;

import static java.util.Objects.isNull;

public class DefaultMasker implements Masker {

  @Override
  public String getType() {
    return "DEFAULT";
  }

  @Override
  public String mask(String input) {
    if (isNull(input)) {
      return null;
    }
    return "*";
  }
}
