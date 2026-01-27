package org.kockpit.audit.httpexchange;

import org.kockpit.audit.api.AuditorEventService;
import org.kockpit.audit.httpexchange.obfuscator.AuditObfuscator;
import org.kockpit.audit.httpexchange.obfuscator.AuditedResponseTooLargeObfuscator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
@ConditionalOnClass(RestTemplate.class)
public class HttpExchangeAutoConfiguration {

  @Value("${kockpit.audit.http.authorization.header.filter:true}")
  private boolean filterAuthorizationHeader;

  @Bean
  @ConditionalOnProperty(
      prefix = "kockpit.audit.http",
      name = "disabled",
      havingValue = "false",
      matchIfMissing = true)
  AuditedResponseTooLargeObfuscator auditedResponseTooLargeObfuscator() {
    return new AuditedResponseTooLargeObfuscator();
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "kockpit.audit.http",
      name = "disabled",
      havingValue = "false",
      matchIfMissing = true)
  @Order
  RestTemplateCustomizer auditRestTemplateCustomizer(
      AuditorEventService auditorEventService,
      @Autowired(required = false) List<AuditObfuscator> bodyAuditObfuscators) {
    return new AuditRestTemplateCustomizer(
        auditClientHttpRequestInterceptor(auditorEventService, bodyAuditObfuscators));
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "kockpit.audit.http",
      name = "disabled",
      havingValue = "false",
      matchIfMissing = true)
  AuditClientHttpRequestInterceptor auditClientHttpRequestInterceptor(
      AuditorEventService auditorEventService,
      @Autowired(required = false) List<AuditObfuscator> bodyAuditObfuscators) {
    return new AuditClientHttpRequestInterceptor(auditorEventService, bodyAuditObfuscators, filterAuthorizationHeader);
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "kockpit.audit.http",
      name = "disabled",
      havingValue = "false",
      matchIfMissing = true)
  AuditRestTemplateBeanPostProcessor auditRestTemplateBeanPostProcessor(
      AuditorEventService auditorEventService,
      @Autowired(required = false) List<AuditObfuscator> bodyAuditObfuscators) {
    return new AuditRestTemplateBeanPostProcessor(auditorEventService, bodyAuditObfuscators, filterAuthorizationHeader);
  }
}
