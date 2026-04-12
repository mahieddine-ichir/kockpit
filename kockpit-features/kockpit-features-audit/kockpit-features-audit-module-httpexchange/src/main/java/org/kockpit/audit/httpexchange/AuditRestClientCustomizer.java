package org.kockpit.audit.httpexchange;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.web.client.RestClient;

public class AuditRestClientCustomizer implements RestClientCustomizer {

  private final AuditClientHttpRequestInterceptor interceptor;

  public AuditRestClientCustomizer(AuditClientHttpRequestInterceptor interceptor) {
    this.interceptor = interceptor;
  }

  @Override
  public void customize(RestClient.Builder restClientBuilder) {
    restClientBuilder.requestInterceptor(interceptor);
  }
}