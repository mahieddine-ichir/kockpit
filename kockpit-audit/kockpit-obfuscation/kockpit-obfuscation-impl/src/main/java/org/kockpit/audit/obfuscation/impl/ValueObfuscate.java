package org.kockpit.audit.obfuscation.impl;

import org.kockpit.audit.obfuscation.Obfuscate;
import org.kockpit.audit.obfuscation.impl.obfuscators.value.ValueObfuscateConfig;
import org.kockpit.audit.obfuscation.masker.MaskerService;

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
