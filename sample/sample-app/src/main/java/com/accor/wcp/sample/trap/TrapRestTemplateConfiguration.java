package com.accor.wcp.sample.trap;

import com.accor.wcp.sample.dynaconfig.ApplicationProperties;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TrapRestTemplateConfiguration {

  @Bean
  @Qualifier("fakeRestTemplate")
  public RestTemplate fakeRestTemplate(
      @Qualifier("fakeHttpRequestFactory") ClientHttpRequestFactory httpFactory,
      RestTemplateBuilder restTemplateBuilder,
      ApplicationProperties properties) {
    ClientHttpRequestInterceptor clientHttpRequestInterceptor =
        (req, resp, filterChain) -> {
          req.getHeaders().add(HttpHeaders.AUTHORIZATION, "FAKE");
          return filterChain.execute(req, resp);
        };
    return restTemplateBuilder
        .errorHandler(new DefaultResponseErrorHandler())
        .requestFactory(() -> new BufferingClientHttpRequestFactory(httpFactory))
        .interceptors(
            List.of(clientHttpRequestInterceptor, new CustomHeaderInterceptor(properties)))
        .build();
  }

  @Bean
  @Qualifier("fakeHttpRequestFactory")
  public ClientHttpRequestFactory fakeClientHttpRequestFactory() {
    return new SimpleClientHttpRequestFactory();
  }
}
