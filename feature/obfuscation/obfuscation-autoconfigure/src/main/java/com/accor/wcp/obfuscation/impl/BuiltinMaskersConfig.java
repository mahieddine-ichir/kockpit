package com.accor.wcp.obfuscation.impl;

import com.accor.wcp.obfuscation.impl.masker.maskers.DefaultMasker;
import com.accor.wcp.obfuscation.impl.masker.maskers.EmailMasker;
import com.accor.wcp.obfuscation.impl.masker.maskers.KeepFirstNbCharsMasker;
import com.accor.wcp.obfuscation.impl.masker.maskers.KeepLastNbCharsMasker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class BuiltinMaskersConfig {

  @Bean
  @ConditionalOnMissingBean
  DefaultMasker defaultMasker() {
    return new DefaultMasker();
  }

  @Bean
  @ConditionalOnMissingBean
  EmailMasker emailMasker() {
    return new EmailMasker();
  }

  @Bean
  KeepFirstNbCharsMasker keepFirst1CharsMasker() {
    return new KeepFirstNbCharsMasker(1);
  }

  @Bean
  KeepFirstNbCharsMasker keepFirst2CharsMasker() {
    return new KeepFirstNbCharsMasker(2);
  }

  @Bean
  KeepFirstNbCharsMasker keepFirst3CharsMasker() {
    return new KeepFirstNbCharsMasker(3);
  }

  @Bean
  KeepFirstNbCharsMasker keepFirst4CharsMasker() {
    return new KeepFirstNbCharsMasker(4);
  }

  @Bean
  KeepLastNbCharsMasker keepLast1CharsMasker() {
    return new KeepLastNbCharsMasker(1);
  }

  @Bean
  KeepLastNbCharsMasker keepLast2CharsMasker() {
    return new KeepLastNbCharsMasker(2);
  }

  @Bean
  KeepLastNbCharsMasker keepLast3CharsMasker() {
    return new KeepLastNbCharsMasker(3);
  }

  @Bean
  KeepLastNbCharsMasker keepLast4CharsMasker() {
    return new KeepLastNbCharsMasker(4);
  }
}
