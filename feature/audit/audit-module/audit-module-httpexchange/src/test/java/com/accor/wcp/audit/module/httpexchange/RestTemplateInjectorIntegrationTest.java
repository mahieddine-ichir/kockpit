package com.accor.wcp.audit.module.httpexchange;

import com.accor.wcp.audit.AuditEvent;
import com.accor.wcp.audit.AuditorEventService;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static com.accor.wcp.audit.module.httpexchange.AuditClientHttpRequestInterceptor.RESTTEMPLATE_MISSING_BUFFERING_CLIENT_HTTP_REQUEST_FACTORY;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.http.HttpMethod.POST;

// Disable if breaks in container
@ExtendWith(MockitoExtension.class)
public class RestTemplateInjectorIntegrationTest {

  @Mock private AuditorEventService auditorEvents;

  @Captor private ArgumentCaptor<List<AuditEvent>> argumentCaptor;

  private RestTemplateInterceptorInjector underTest;

  @BeforeAll
  public static void startServer() {
    TestMockServer.createExpectationServer();
  }

  @AfterAll
  public static void stopServer() {
    TestMockServer.stop();
  }

  @BeforeEach
  public void setup() {
    AuditClientHttpRequestInterceptor interceptor =
        new AuditClientHttpRequestInterceptor(auditorEvents, new ArrayList<>(), true);
    underTest = new RestTemplateInterceptorInjector(interceptor);
  }

  @Test
  void should_intercept_vanilla_resttemplate() {
    RestTemplate restTemplate = new RestTemplate();
    underTest.inject(restTemplate);
    assertThat(restTemplate.getInterceptors()).hasSize(1);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<String> exchange =
        restTemplate.exchange(
            "http://127.0.0.1:8055/test",
            POST,
            new HttpEntity<>("{username: 'foo', password: 'bar'}", headers),
            String.class);
    AssertionsForClassTypes.assertThat(exchange.getBody()).isNotNull();
    Mockito.verify(auditorEvents, Mockito.times(1))
        .addAuditEvents(anyString(), argumentCaptor.capture());
    HttpExchangeAudit httpExchangeAudit = (HttpExchangeAudit) argumentCaptor.getValue().get(0);
    assertThat(httpExchangeAudit.getHttpAuditedResponse().getPayload())
        .isEqualTo(RESTTEMPLATE_MISSING_BUFFERING_CLIENT_HTTP_REQUEST_FACTORY);
    assertThat(httpExchangeAudit.getHttpAuditedRequest().getBody())
        .isEqualTo("{username: 'foo', password: 'bar'}");
  }

  @Test
  void should_intercept_bufferinghttprequest_resttemplate() {
    RestTemplate restTemplate =
        new RestTemplate(
            new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
    underTest.inject(restTemplate);
    assertThat(restTemplate.getInterceptors()).hasSize(1);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<String> exchange =
        restTemplate.exchange(
            "http://127.0.0.1:8055/test",
            POST,
            new HttpEntity<>("{username: 'foo', password: 'bar'}", headers),
            String.class);
    AssertionsForClassTypes.assertThat(exchange.getBody()).isNotNull();
    Mockito.verify(auditorEvents, Mockito.times(1))
        .addAuditEvents(anyString(), argumentCaptor.capture());
    HttpExchangeAudit httpExchangeAudit = (HttpExchangeAudit) argumentCaptor.getValue().get(0);
    assertThat(httpExchangeAudit.getHttpAuditedResponse().getPayload())
        .isEqualTo("{ message: 'incorrect username and password combination' }");
    assertThat(httpExchangeAudit.getHttpAuditedRequest().getBody())
        .isEqualTo("{username: 'foo', password: 'bar'}");
  }
}
