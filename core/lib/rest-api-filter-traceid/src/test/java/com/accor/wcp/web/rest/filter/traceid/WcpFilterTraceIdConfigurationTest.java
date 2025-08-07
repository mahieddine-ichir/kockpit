package com.accor.wcp.web.rest.filter.traceid;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

class WcpFilterTraceIdConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner().withUserConfiguration(WcpFilterTraceIdConfiguration.class);

  @Test
  void shouldInstantiateOnlyWcpTraceIdManagerFilterRegistrationBeanWhenActivationFlagIsNotExist() {
    contextRunner
        .withPropertyValues()
        .run(
            context -> {
              assertThat(context).hasSingleBean(FilterRegistrationBean.class);
              assertThat(context).doesNotHaveBean("wcpTraceIdManagerFilterRegistrationBean");
              assertThat(context).hasBean("wcpXB3TraceIdManagerFilterRegistrationBean");
            });
  }

  @Test
  void shouldInstantiateOnlyWcpTraceIdManagerFilterRegistrationBeanWhenActivationFlagIsFalse() {
    contextRunner
        .withPropertyValues("wcp.web.rest.api.header.traceid.retrocompatibilty=false")
        .run(
            context -> {
              assertThat(context).hasSingleBean(FilterRegistrationBean.class);
              assertThat(context).doesNotHaveBean("wcpTraceIdManagerFilterRegistrationBean");
              assertThat(context).hasBean("wcpXB3TraceIdManagerFilterRegistrationBean");
            });
  }

  @Test
  void shouldInstantiateOnlyXB3TraceIdManagerFilterRegistrationBeanWhenActivationFlagIsTrue() {
    contextRunner
        .withPropertyValues("wcp.web.rest.api.header.traceid.retrocompatibilty=true")
        .run(
            context -> {
              assertThat(context).hasSingleBean(FilterRegistrationBean.class);
              assertThat(context).doesNotHaveBean("wcpXB3TraceIdManagerFilterRegistrationBean");
              assertThat(context).hasBean("wcpTraceIdManagerFilterRegistrationBean");
            });
  }
}
