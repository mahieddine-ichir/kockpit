package com.accor.wcp.web.rest.filter.traceid;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

class WcpFilterTraceIdConfiguration {

  @Bean
  @ConditionalOnProperty(
      value = "wcp.web.rest.api.header.traceid.retrocompatibilty",
      havingValue = "true")
  /**
   * @deprecated replaced by {@link
   *     WcpFilterTraceIdConfiguration#wcpXB3TraceIdManagerFilterRegistrationBean(String)} }
   */
  @Deprecated
  public FilterRegistrationBean<WcpTraceIdManagerFilter> wcpTraceIdManagerFilterRegistrationBean(
      @Value("${wcp.web.incoming.header.traceid:X-WCP-TraceId}") String traceIdHeaderName) {
    FilterRegistrationBean<WcpTraceIdManagerFilter> registrationBean =
        new FilterRegistrationBean<>();
    WcpTraceIdManagerFilter wcpTraceIdManagerFilter =
        new WcpTraceIdManagerFilter(traceIdHeaderName);
    registrationBean.setFilter(wcpTraceIdManagerFilter);
    registrationBean.setOrder(HIGHEST_PRECEDENCE + 1);
    return registrationBean;
  }

  @Bean
  @ConditionalOnProperty(
      value = "wcp.web.rest.api.header.traceid.retrocompatibilty",
      matchIfMissing = true,
      havingValue = "false")
  public FilterRegistrationBean<WcpXB3TraceIdManagerFilter>
      wcpXB3TraceIdManagerFilterRegistrationBean(
          @Value("${wcp.web.incoming.header.traceid:X-B3-TraceId}") String traceIdHeaderName) {
    FilterRegistrationBean<WcpXB3TraceIdManagerFilter> registrationBean =
        new FilterRegistrationBean<>();
    WcpXB3TraceIdManagerFilter wcpXB3TraceIdManagerFilter =
        new WcpXB3TraceIdManagerFilter(traceIdHeaderName);
    registrationBean.setFilter(wcpXB3TraceIdManagerFilter);
    registrationBean.setOrder(HIGHEST_PRECEDENCE + 1);
    return registrationBean;
  }
}
