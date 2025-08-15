package com.accor.wcp.services.auditstream.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return (web) ->
        web.ignoring()
            .requestMatchers(HttpMethod.OPTIONS, "/**")
            .requestMatchers("/actuator/**")
            .requestMatchers("/swagger-ui/**")
            .requestMatchers("/swagger-resources/**")
            .requestMatchers("/v3/api-docs/**");
  }
}
