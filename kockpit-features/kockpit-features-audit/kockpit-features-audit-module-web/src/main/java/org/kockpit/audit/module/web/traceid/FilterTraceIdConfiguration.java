package org.kockpit.audit.module.web.traceid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Configuration
class FilterTraceIdConfiguration {

  @Bean
  @ConditionalOnProperty(
      value = "kockpit.sdk.service.audit.web.rest.api.tracing",
      matchIfMissing = true,
      havingValue = "false")
  public FilterRegistrationBean<XB3TraceIdManagerFilter>
      wcpXB3TraceIdManagerFilterRegistrationBean(
          @Value("${kockpit.sdk.service.audit.web.rest.api.header.traceid:X-B3-TraceId}") String traceIdHeaderName) {
    FilterRegistrationBean<XB3TraceIdManagerFilter> registrationBean =
        new FilterRegistrationBean<>();
      XB3TraceIdManagerFilter wcpXB3TraceIdManagerFilter =
        new XB3TraceIdManagerFilter(traceIdHeaderName);
    registrationBean.setFilter(wcpXB3TraceIdManagerFilter);
    registrationBean.setOrder(HIGHEST_PRECEDENCE + 1);
    return registrationBean;
  }
}
