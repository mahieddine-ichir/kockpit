package org.kockpit.audit.obfuscate;

import org.kockpit.audit.AuditObfuscationService;
import org.kockpit.audit.api.AuditReport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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
    return new AuditObfuscationService() {
      @Override
      public void obfuscate(AuditReport auditReport) {
        // do nothing
      }
    };
  }
}
