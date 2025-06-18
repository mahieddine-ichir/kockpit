package org.kockpit.audit.obfuscation.impl;

import org.kockpit.audit.obfuscation.Obfuscate;
import org.kockpit.audit.obfuscation.ObfuscationService;
import org.kockpit.audit.obfuscation.impl.masker.MaskerServiceImpl;
import org.kockpit.audit.obfuscation.masker.Masker;
import org.kockpit.audit.obfuscation.masker.MaskerService;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ObfuscationConfig {

  @ConditionalOnMissingBean
  @Bean
  MaskerService maskerService(List<Masker> maskers) {
    return new MaskerServiceImpl(maskers);
  }

  @ConditionalOnMissingBean
  @Bean
  ObfuscationService obfuscationService(List<Obfuscate<?>> obfuscates) {
    return new ObfuscationServiceImpl(obfuscates);
  }
}
