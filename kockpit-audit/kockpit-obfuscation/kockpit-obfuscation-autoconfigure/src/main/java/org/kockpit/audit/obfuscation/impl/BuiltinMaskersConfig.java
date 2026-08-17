package org.kockpit.audit.obfuscation.impl;

import org.springframework.boot.autoconfigure.AutoConfiguration;

import org.kockpit.audit.obfuscation.impl.masker.maskers.DefaultMasker;
import org.kockpit.audit.obfuscation.impl.masker.maskers.EmailMasker;
import org.kockpit.audit.obfuscation.impl.masker.maskers.KeepFirstNbCharsMasker;
import org.kockpit.audit.obfuscation.impl.masker.maskers.KeepLastNbCharsMasker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
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
