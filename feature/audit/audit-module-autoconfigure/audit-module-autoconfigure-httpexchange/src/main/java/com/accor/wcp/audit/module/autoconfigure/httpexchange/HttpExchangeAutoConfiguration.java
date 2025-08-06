package com.accor.wcp.audit.module.autoconfigure.httpexchange;

import com.accor.wcp.audit.AuditorEventService;
import com.accor.wcp.audit.module.httpexchange.AuditClientHttpRequestInterceptor;
import com.accor.wcp.audit.module.httpexchange.AuditRestTemplateBeanPostProcessor;
import com.accor.wcp.audit.module.httpexchange.AuditRestTemplateCustomizer;
import com.accor.wcp.audit.module.httpexchange.obfuscator.AuditObfuscator;
import com.accor.wcp.audit.module.httpexchange.obfuscator.AuditedResponseTooLargeObfuscator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnClass(RestTemplate.class)
public class HttpExchangeAutoConfiguration {

  @Value("${wcp.sdk.service.audit.http.authorization.header.filter:true}")
  private boolean filterAuthorizationHeader;

  @Bean
  @ConditionalOnProperty(
      prefix = "wcp.sdk.service.audit.http",
      name = "disabled",
      havingValue = "false",
      matchIfMissing = true)
  AuditedResponseTooLargeObfuscator auditedResponseTooLargeObfuscator() {
    return new AuditedResponseTooLargeObfuscator();
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "wcp.sdk.service.audit.http",
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
      prefix = "wcp.sdk.service.audit.http",
      name = "disabled",
      havingValue = "false",
      matchIfMissing = true)
  AuditClientHttpRequestInterceptor auditClientHttpRequestInterceptor(
      AuditorEventService auditorEventService,
      @Autowired(required = false) List<AuditObfuscator> bodyAuditObfuscators) {
    return new AuditClientHttpRequestInterceptor(
        auditorEventService, bodyAuditObfuscators, filterAuthorizationHeader);
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "wcp.sdk.service.audit.http",
      name = "disabled",
      havingValue = "false",
      matchIfMissing = true)
  AuditRestTemplateBeanPostProcessor auditRestTemplateBeanPostProcessor(
      AuditorEventService auditorEventService,
      @Autowired(required = false) List<AuditObfuscator> bodyAuditObfuscators) {
    return new AuditRestTemplateBeanPostProcessor(
        auditorEventService, bodyAuditObfuscators, filterAuthorizationHeader);
  }
}
