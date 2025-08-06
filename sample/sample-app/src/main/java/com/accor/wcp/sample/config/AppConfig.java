package com.accor.wcp.sample.config;

import com.accor.wcp.sdk.application.service.dynaconfig.DynaConfigAttribute;
import com.accor.wcp.sdk.application.service.dynaconfig.DynaConfigEnabler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

@Configuration(proxyBeanMethods = false)
@DynaConfigEnabler
public class AppConfig {

  @Value("${dyna.property.backend.call.timeout:10}")
  @DynaConfigAttribute(beanPropertyAccess = true)
  private Integer defaultBackendCallTimeout;

  private RestTemplate restTemplate;
  private SimpleClientHttpRequestFactory simpleClientHttpRequestFactory;

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
    simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
    simpleClientHttpRequestFactory.setConnectTimeout(defaultBackendCallTimeout);
    simpleClientHttpRequestFactory.setReadTimeout(defaultBackendCallTimeout);
    restTemplate =
        restTemplateBuilder
            .errorHandler(new DefaultResponseErrorHandler())
            .requestFactory(
                () -> new BufferingClientHttpRequestFactory(simpleClientHttpRequestFactory))
            .build();
    return restTemplate;
  }

  public void setDefaultBackendCallTimeout(Integer defaultBackendCallTimeout) {
    this.defaultBackendCallTimeout = defaultBackendCallTimeout;
    simpleClientHttpRequestFactory.setConnectTimeout(defaultBackendCallTimeout);
    simpleClientHttpRequestFactory.setReadTimeout(defaultBackendCallTimeout);
  }

  @Bean
  WebClient webClient(Builder webClientBuilder) {
    return webClientBuilder.build();
  }
}
