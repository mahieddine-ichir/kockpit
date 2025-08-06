package com.accor.wcp.audit.module.httpexchange;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@ExtendWith(MockitoExtension.class)
class RestTemplateInterceptorInjectorTest {

  @Mock private ClientHttpRequestInterceptor clientHttpRequestInterceptor;
  @InjectMocks private RestTemplateInterceptorInjector underTest;

  @Test
  void should_inject_for_intercepting_client_http_request_factory() {
    RestTemplate restTemplate = getRestTemplate();
    underTest.inject(restTemplate);
    assertThat(restTemplate.getInterceptors()).contains(clientHttpRequestInterceptor);
  }

  @Test
  void should_inject_no_intercepting_client_http_request_factory() {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setRequestFactory(
        new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
    underTest.inject(restTemplate);
    assertThat(restTemplate.getInterceptors()).contains(clientHttpRequestInterceptor);
  }

  @Test
  void should_inject_vanilla_rest_template() {
    RestTemplate restTemplate = new RestTemplate();
    underTest.inject(restTemplate);
    assertThat(restTemplate.getInterceptors()).contains(clientHttpRequestInterceptor);
  }

  private RestTemplate getRestTemplate() {
    List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
    interceptors.add(new BasicAuthenticationInterceptor("user", "password"));
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.setRequestFactory(
        new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
    restTemplate.setInterceptors(interceptors);
    restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("localhost"));
    return restTemplate;
  }
}
