package com.accor.wcp.console.services.config;

import com.accor.wcp.console.services.core.security.ServiceEndpointGuardFilter;
import com.accor.wcp.console.services.core.servicemanager.ServiceManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@EnableWebSecurity
@Configuration
@Order(10)
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
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

  private final ServiceManager serviceManager;

  @Value("${console.services.internal-public-prefix}")
  private String internalPublicServicesPath;

  @Value("${console.services.public-prefix}")
  private String publicServicesPath;

  @Bean
  SecurityFilterChain httpSecurityFilter(HttpSecurity http) throws Exception {
    http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> {
            authorize.requestMatchers(internalPublicServicesPath + "/**").permitAll();
            authorize.requestMatchers(publicServicesPath + "/**").permitAll();
            authorize.requestMatchers(HttpMethod.OPTIONS, "/*").permitAll();
            authorize.requestMatchers("/me").permitAll().anyRequest().authenticated();
        })
        .addFilterAfter(new ServiceEndpointGuardFilter(serviceManager), AuthorizationFilter.class)
        .oauth2ResourceServer(configurer -> configurer.jwt(jwtConfigurer -> {}))
        .csrf(AbstractHttpConfigurer::disable);

    return http.getOrBuild();
  }
}
