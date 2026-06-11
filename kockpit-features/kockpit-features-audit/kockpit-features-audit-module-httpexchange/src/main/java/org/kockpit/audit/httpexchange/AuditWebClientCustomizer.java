package org.kockpit.audit.httpexchange;

import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.web.reactive.function.client.WebClient;

public class AuditWebClientCustomizer implements WebClientCustomizer {

  private final AuditExchangeFilterFunction filter;

  public AuditWebClientCustomizer(AuditExchangeFilterFunction filter) {
    this.filter = filter;
  }

  @Override
  public void customize(WebClient.Builder webClientBuilder) {
    webClientBuilder.filter(filter);
  }
}
