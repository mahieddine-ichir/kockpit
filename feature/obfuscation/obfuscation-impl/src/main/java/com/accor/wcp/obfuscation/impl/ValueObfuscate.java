package com.accor.wcp.obfuscation.impl;

import com.accor.wcp.obfuscation.Obfuscate;
import com.accor.wcp.obfuscation.impl.obfuscators.value.ValueObfuscateConfig;
import com.accor.wcp.obfuscation.masker.MaskerService;

class ValueObfuscate implements Obfuscate<ValueObfuscateConfig> {

  private final MaskerService maskerService;

  public ValueObfuscate(MaskerService maskerService) {
    this.maskerService = maskerService;
  }

  @Override
  public String doObfuscate(String data, ValueObfuscateConfig config) {
    return maskerService.mask(data, config.getMaskerId());
  }
}
