package com.accor.wcp.web.rest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfiguration {

  @Autowired private ServerProperties serverProperties;

  @Bean
  public AccessLogConfig accessLogConfig() {
    return new AccessLogConfig(serverProperties);
  }
}
