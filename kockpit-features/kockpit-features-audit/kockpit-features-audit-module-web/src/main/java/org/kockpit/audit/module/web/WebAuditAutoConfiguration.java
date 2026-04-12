package org.kockpit.audit.module.web;

import org.kockpit.audit.api.AuditorEventService;
import org.kockpit.audit.api.AuditorKeyValueService;
import org.kockpit.audit.api.AuditorService;
import org.kockpit.audit.module.web.request.DefaultRequestAuditor;
import org.kockpit.audit.module.web.request.HeadersRequestAuditor;
import org.kockpit.audit.module.web.request.ParametersRequestAuditor;
import org.kockpit.audit.module.web.response.DefaultResponseAuditor;
import org.kockpit.audit.module.web.response.HeadersResponseAuditor;
import org.kockpit.audit.module.web.skipped.ActuatorSkipAuditor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@ConditionalOnProperty(
        value = "kockpit.audit.enabled",
        havingValue = "true"
)
@Configuration
public class WebAuditAutoConfiguration {

  @Value("${kockpit.audit.http.origin.header:X-KOCKPIT-Origin}")
  private String originHeaderName;

  @Value("${kockpit.audit.http.authorization.header.filter:false}")
  private boolean filterAuthorizationHeader;

  @Bean
  public SkipRequestAuditor actuatorSkipRequestAudit() {
    return new ActuatorSkipAuditor();
  }

  @Bean
  public RequestAuditor defaultRequestAuditor() {
    return new DefaultRequestAuditor(originHeaderName, filterAuthorizationHeader);
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "kockpit.audit.http.parameters",
      name = "enabled",
      havingValue = "true")
  public RequestAuditor parametersRequestAuditor() {
    return new ParametersRequestAuditor();
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "kockpit.audit.http.request.headers",
      name = "enabled",
      havingValue = "true")
  public RequestAuditor headersRequestAuditor(
      @Value("${kockpit.audit.http.request.headers.names}")
          List<String> requestHeaderNames) {
    return new HeadersRequestAuditor(requestHeaderNames);
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "kockpit.audit.http.response.headers",
      name = "enabled",
      havingValue = "true")
  public ResponseAuditor headersResponseAuditor(
      @Value("${kockpit.audit.http.response.headers.names}")
          List<String> requestHeaderNames) {
    return new HeadersResponseAuditor(requestHeaderNames);
  }

  @Bean
  public ResponseAuditor defaultResponseAuditor() {
    return new DefaultResponseAuditor(filterAuthorizationHeader);
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "kockpit.audit.web",
      name = "disabled",
      havingValue = "false",
      matchIfMissing = true)
  public AuditFilter auditFilter(
      List<SkipRequestAuditor> skipRequestAuditorList,
      List<RequestAuditor> requestAuditorList,
      List<ResponseAuditor> responseAuditorList,
      AuditorService auditorService,
      AuditorEventService auditorEventService,
      AuditorKeyValueService auditorKeyValueService) {
    return new AuditFilter(
        skipRequestAuditorList,
        requestAuditorList,
        responseAuditorList,
        auditorService,
        auditorEventService,
        auditorKeyValueService);
  }

//  // fixme to a security starter
//  @Bean
//  @ConditionalOnBean(AuditFilter.class)
//  public SecurityAuditFilter securityAuditFilter(AuditFilter auditFilter) {
//    return new SecurityAuditFilter(auditFilter);
//  }
}
