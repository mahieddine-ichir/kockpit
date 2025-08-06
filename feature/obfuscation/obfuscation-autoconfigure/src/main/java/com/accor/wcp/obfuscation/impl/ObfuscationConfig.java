package com.accor.wcp.obfuscation.impl;

import com.accor.wcp.obfuscation.Obfuscate;
import com.accor.wcp.obfuscation.ObfuscationService;
import com.accor.wcp.obfuscation.impl.masker.MaskerServiceImpl;
import com.accor.wcp.obfuscation.masker.Masker;
import com.accor.wcp.obfuscation.masker.MaskerService;
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
