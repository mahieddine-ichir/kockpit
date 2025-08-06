package com.accor.wcp.audit.module.web;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.accor.wcp.audit.AuditorEventService;
import com.accor.wcp.audit.AuditorKeyValueService;
import com.accor.wcp.audit.AuditorService;
import com.accor.wcp.audit.module.web.request.DefaultRequestAuditor;
import com.accor.wcp.audit.module.web.request.HeadersRequestAuditor;
import com.accor.wcp.audit.module.web.request.ParametersRequestAuditor;
import com.accor.wcp.audit.module.web.response.DefaultResponseAuditor;
import com.accor.wcp.audit.module.web.response.HeadersResponseAuditor;
import com.accor.wcp.audit.module.web.skipped.ActuatorSkipAuditor;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebAuditAutoConfiguration {

  @Value("${wcp.sdk.service.audit.http.origin.header:}")
  private String originHeaderName;

  @Value("${wcp.sdk.service.audit.http.traceid.header:}")
  private String traceIdHeaderName;

  @Value("${wcp.sdk.service.audit.http.authorization.header.filter:true}")
  private boolean filterAuthorizationHeader;

  @Bean
  public SkipRequestAuditor actuatorSkipRequestAudit() {
    return new ActuatorSkipAuditor();
  }

  @Bean
  public RequestAuditor defaultRequestAuditor() {
    originHeaderName =
        isNotBlank(originHeaderName) ? originHeaderName : DefaultHeaderNames.WCP_ORIGIN;
    traceIdHeaderName =
        isNotBlank(traceIdHeaderName) ? traceIdHeaderName : DefaultHeaderNames.WCP_TRACE_ID;
    return new DefaultRequestAuditor(
        originHeaderName, traceIdHeaderName, filterAuthorizationHeader);
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "wcp.sdk.service.audit.http.parameters",
      name = "enabled",
      havingValue = "true")
  public RequestAuditor parametersRequestAuditor() {
    return new ParametersRequestAuditor();
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "wcp.sdk.service.audit.http.request.headers",
      name = "enabled",
      havingValue = "true")
  public RequestAuditor headersRequestAuditor(
      @Value("${wcp.sdk.service.audit.http.request.headers.names}")
          List<String> requestHeaderNames) {
    return new HeadersRequestAuditor(requestHeaderNames);
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "wcp.sdk.service.audit.http.response.headers",
      name = "enabled",
      havingValue = "true")
  public ResponseAuditor headersResponseAuditor(
      @Value("${wcp.sdk.service.audit.http.response.headers.names}")
          List<String> requestHeaderNames) {
    return new HeadersResponseAuditor(requestHeaderNames);
  }

  @Bean
  public ResponseAuditor defaultResponseAuditor() {
    return new DefaultResponseAuditor(filterAuthorizationHeader);
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "wcp.sdk.service.audit.web",
      name = "disabled",
      havingValue = "false",
      matchIfMissing = true)
  public AuditFilter auditFilter(
      List<SkipRequestAuditor> skipRequestAuditorList,
      List<RequestAuditor> requestAuditorList,
      List<ResponseAuditor> responseAuditorList,
      AuditorService wcpAuditor,
      AuditorEventService auditorEventService,
      AuditorKeyValueService auditorKeyValueService) {
    return new AuditFilter(
        skipRequestAuditorList,
        requestAuditorList,
        responseAuditorList,
        wcpAuditor,
        auditorEventService,
        auditorKeyValueService);
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "wcp.sdk.service.audit.web",
      name = "disabled",
      havingValue = "false",
      matchIfMissing = true)
  public SecurityAuditFilter securityAuditFilter(AuditFilter auditFilter) {
    return new SecurityAuditFilter(auditFilter);
  }
}
