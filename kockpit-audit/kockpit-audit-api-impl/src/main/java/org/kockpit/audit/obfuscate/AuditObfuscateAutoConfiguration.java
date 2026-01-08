package org.kockpit.audit.obfuscate;

import org.kockpit.audit.AuditObfuscationService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties({AuditObfuscationSettings.class})
class AuditObfuscateAutoConfiguration {

  /*
  @Bean
  AuditObfuscationService auditObfuscationService(
          ObfuscationService obfuscationService, AuditObfuscationSettings auditObfuscationSettings) {
    return new AuditObfuscationServiceImpl(obfuscationService, auditObfuscationSettings);
  }
   */

  @Bean
  @ConditionalOnMissingBean
  AuditObfuscationService nopeObfuscationService() {
    return auditReport -> {
      // do nothing
    };
  }
}
