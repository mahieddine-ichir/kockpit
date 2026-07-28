package org.kockpit.audit.httpexchange;

import org.springframework.boot.autoconfigure.AutoConfiguration;

import org.kockpit.audit.api.AuditorEventService;
import org.kockpit.audit.api.AuditorService;
import org.kockpit.audit.httpexchange.obfuscator.AuditObfuscator;
import org.kockpit.audit.httpexchange.obfuscator.AuditedResponseTooLargeObfuscator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.boot.restclient.RestTemplateCustomizer;
import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@AutoConfiguration
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

  @Configuration
  @ConditionalOnClass(RestTemplate.class)
  class RestTemplateAuditConfiguration {

    @Bean
    @ConditionalOnProperty(
            prefix = "kockpit.audit.http",
            name = "disabled",
            havingValue = "false",
            matchIfMissing = true)
    @ConditionalOnBean(AuditorEventService.class)
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
    @ConditionalOnBean(AuditorEventService.class)
    AuditClientHttpRequestInterceptor auditClientHttpRequestInterceptor(
            AuditorEventService auditorEventService,
            @Autowired(required = false) List<AuditObfuscator> bodyAuditObfuscators) {
      return new AuditClientHttpRequestInterceptor(
              auditorEventService, bodyAuditObfuscators, filterAuthorizationHeader);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "kockpit.audit.http",
            name = "disabled",
            havingValue = "false",
            matchIfMissing = true)
    @ConditionalOnBean(AuditorEventService.class)
    AuditRestTemplateBeanPostProcessor auditRestTemplateBeanPostProcessor(
            AuditorEventService auditorEventService,
            @Autowired(required = false) List<AuditObfuscator> bodyAuditObfuscators) {
      return new AuditRestTemplateBeanPostProcessor(
              auditorEventService, bodyAuditObfuscators, filterAuthorizationHeader);
    }
  }

  @Configuration
  @ConditionalOnClass(WebClient.class)
  @ConditionalOnBean(AuditorEventService.class)
  class WebClientAuditConfiguration {

    @Bean
    @ConditionalOnProperty(
            prefix = "kockpit.audit.http",
            name = "disabled",
            havingValue = "false",
            matchIfMissing = true)
    WebClientCustomizer auditWebClientCustomizer(
            AuditorEventService auditorEventService,
            AuditorService auditorService,
            @Autowired(required = false) List<AuditObfuscator> bodyAuditObfuscators) {
      return new AuditWebClientCustomizer(
              new AuditExchangeFilterFunction(
                      auditorEventService, auditorService, bodyAuditObfuscators, filterAuthorizationHeader));
    }
  }

  @Configuration
  @ConditionalOnClass(RestClient.class)
  @ConditionalOnBean(AuditorEventService.class)
  class RestClientAuditConfiguration {

    @Bean
    @ConditionalOnProperty(
            prefix = "kockpit.audit.http",
            name = "disabled",
            havingValue = "false",
            matchIfMissing = true)
    @Order
    RestClientCustomizer auditRestClientCustomizer(
            AuditorEventService auditorEventService,
            @Autowired(required = false) List<AuditObfuscator> bodyAuditObfuscators) {
      return new AuditRestClientCustomizer(
              new AuditClientHttpRequestInterceptor(
                      auditorEventService, bodyAuditObfuscators, filterAuthorizationHeader));
    }
  }
}
