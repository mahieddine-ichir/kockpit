package com.accor.wcp.audit.obfuscate;

import com.accor.wcp.obfuscation.ObfuscationService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AuditObfuscationSettings.class})
class AuditObfuscateAutoConfiguration {

  @Bean
  AuditObfuscationServiceImpl auditObfuscationService(
      ObfuscationService obfuscationService, AuditObfuscationSettings auditObfuscationSettings) {
    return new AuditObfuscationServiceImpl(obfuscationService, auditObfuscationSettings);
  }
}
