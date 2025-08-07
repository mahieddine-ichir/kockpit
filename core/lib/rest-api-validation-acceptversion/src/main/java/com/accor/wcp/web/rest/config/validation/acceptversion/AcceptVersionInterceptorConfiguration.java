package com.accor.wcp.web.rest.config.validation.acceptversion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(value = "wcp.web.rest.api.validation.accept-version.version")
class AcceptVersionInterceptorConfiguration implements WebMvcConfigurer {

  @Value("${wcp.web.rest.api.validation.accept-version.version}")
  private String applicationAcceptVersion;

  @Value("${wcp.web.rest.api.validation.accept-version.header-name:X-Accept-Version}")
  private String acceptVersionHeaderName;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(
        new AcceptVersionInterceptor(applicationAcceptVersion, acceptVersionHeaderName));
  }

  @Bean
  UnsupportedAcceptVersionHandlerController unsupportedAcceptVersionHandlerController() {
    return new UnsupportedAcceptVersionHandlerController();
  }
}
