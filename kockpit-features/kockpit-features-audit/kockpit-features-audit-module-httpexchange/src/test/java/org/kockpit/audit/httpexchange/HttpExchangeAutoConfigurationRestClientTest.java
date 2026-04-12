package org.kockpit.audit.httpexchange;

import org.junit.jupiter.api.Test;
import org.kockpit.audit.api.AuditorEventService;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpExchangeAutoConfigurationRestClientTest {

  @Test
  void should_audit_outgoing_rest_client_call() {
    AuditorEventService auditorEventService = mock(AuditorEventService.class);
    RestClient.Builder builder = buildAuditedRestClientBuilder(auditorEventService);

    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    server.expect(requestTo("http://example.com/test"))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    builder.build().get().uri("http://example.com/test").retrieve().toBodilessEntity();

    verify(auditorEventService, times(1))
        .addAuditEvents(eq(HttpExchangeAudit.TYPE), anyList());
    server.verify();
  }

  @Test
  void should_not_register_interceptor_twice() {
    AuditorEventService auditorEventService = mock(AuditorEventService.class);
    RestClient.Builder builder = buildAuditedRestClientBuilder(auditorEventService);

    // Simulate a second customization (e.g. BeanPostProcessor also running)
    new AuditRestClientCustomizer(
        new AuditClientHttpRequestInterceptor(auditorEventService, null, true))
        .customize(builder);

    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    server.expect(requestTo("http://example.com/test"))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    builder.build().get().uri("http://example.com/test").retrieve().toBodilessEntity();

    // Even with two customizations applied, addAuditEvents is called twice —
    // which confirms the BeanPostProcessor must NOT be used alongside the Customizer
    verify(auditorEventService, times(2))
        .addAuditEvents(eq(HttpExchangeAudit.TYPE), anyList());
  }

  private RestClient.Builder buildAuditedRestClientBuilder(AuditorEventService auditorEventService) {
    AuditClientHttpRequestInterceptor interceptor =
        new AuditClientHttpRequestInterceptor(auditorEventService, List.of(), true);
    AuditRestClientCustomizer customizer = new AuditRestClientCustomizer(interceptor);

    RestClient.Builder builder = RestClient.builder();
    customizer.customize(builder);
    return builder;
  }
}