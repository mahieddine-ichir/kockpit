package org.kockpit.audit.httpexchange;

import org.springframework.boot.restclient.RestTemplateCustomizer;
import org.springframework.web.client.RestTemplate;

public class AuditRestTemplateCustomizer implements RestTemplateCustomizer {

  private final AuditClientHttpRequestInterceptor interceptor;

  public AuditRestTemplateCustomizer(AuditClientHttpRequestInterceptor interceptor) {
    this.interceptor = interceptor;
  }

  @Override
  public void customize(RestTemplate restTemplate) {
    new RestTemplateInterceptorInjector(this.interceptor).inject(restTemplate);
  }
}
