package com.accor.wcp.obfuscation.impl;

import com.accor.wcp.obfuscation.Obfuscate;

class FakeSimpleValueObfuscate implements Obfuscate<FakeValueObfuscateConfig> {

  @Override
  public String doObfuscate(String data, FakeValueObfuscateConfig config) {
    return "***";
  }
}
