package com.accor.wcp.web.rest.filter.contentlanguage;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.LocaleResolver;

class WcpContentLanguageFilterConfiguration {

  @Bean
  public FilterRegistrationBean<WcpContentLanguageFilter> wcpContentLanguageFilterRegistrationBean(
      LocaleResolver localeResolver) {
    FilterRegistrationBean<WcpContentLanguageFilter> registrationBean =
        new FilterRegistrationBean<>();
    WcpContentLanguageFilter wcpContentLanguageFilter =
        new WcpContentLanguageFilter(localeResolver);
    registrationBean.setFilter(wcpContentLanguageFilter);
    return registrationBean;
  }
}
