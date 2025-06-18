package org.kockpit.audit.obfuscation.impl;

import org.kockpit.audit.obfuscation.masker.MaskerService;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class BuiltinObfuscatesConfig {

  @ConditionalOnMissingBean
  @Bean
  JsonObfuscate jsonObfuscate(MaskerService maskerService) {
    return new JsonObfuscate(maskerService);
  }

  @ConditionalOnMissingBean
  @Bean
  XmlObfuscate xmlObfuscate(MaskerService maskerService) throws ParserConfigurationException {
    return new XmlObfuscate(maskerService);
  }

  @ConditionalOnMissingBean
  @Bean
  ValueObfuscate valueObfuscate(MaskerService maskerService) {
    return new ValueObfuscate(maskerService);
  }
}
