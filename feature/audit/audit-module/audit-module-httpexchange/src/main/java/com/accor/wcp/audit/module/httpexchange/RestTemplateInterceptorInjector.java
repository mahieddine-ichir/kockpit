package com.accor.wcp.audit.module.httpexchange;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class RestTemplateInterceptorInjector {

  private final ClientHttpRequestInterceptor interceptor;

  RestTemplateInterceptorInjector(ClientHttpRequestInterceptor interceptor) {
    this.interceptor = interceptor;
  }

  void inject(RestTemplate restTemplate) {
    if (hasAuditInterceptor(restTemplate)) {
      return;
    }

    List<ClientHttpRequestInterceptor> interceptors =
        new ArrayList<>(restTemplate.getInterceptors());
    interceptors.add(0, this.interceptor);
    restTemplate.setInterceptors(interceptors);
  }

  private boolean hasAuditInterceptor(RestTemplate restTemplate) {
    return restTemplate.getInterceptors().stream()
        .anyMatch(AuditClientHttpRequestInterceptor.class::isInstance);
  }
}
