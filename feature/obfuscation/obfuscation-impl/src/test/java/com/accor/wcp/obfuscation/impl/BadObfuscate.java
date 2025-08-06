package com.accor.wcp.obfuscation.impl;

import com.accor.wcp.obfuscation.Obfuscate;
import com.accor.wcp.obfuscation.ObfuscateConfig;

class BadObfuscate implements Obfuscate<ObfuscateConfig> {

  @Override
  public String doObfuscate(String data, ObfuscateConfig config) {
    return null;
  }
}
