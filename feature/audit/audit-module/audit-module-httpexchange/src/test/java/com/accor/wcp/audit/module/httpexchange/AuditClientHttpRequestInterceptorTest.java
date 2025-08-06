package com.accor.wcp.audit.module.httpexchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.accor.wcp.audit.AuditEvent;
import com.accor.wcp.audit.AuditorEventService;
import com.accor.wcp.audit.module.httpexchange.obfuscator.AuditObfuscator;
import com.accor.wcp.audit.module.httpexchange.obfuscator.RequestBodyAuditObfuscator;
import com.accor.wcp.audit.module.httpexchange.obfuscator.ResponseBodyAuditObfuscator;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;

@ExtendWith(MockitoExtension.class)
class AuditClientHttpRequestInterceptorTest {

  @Mock AuditorEventService auditorEvents;
  @Captor ArgumentCaptor<List<AuditEvent>> exchangeReportCaptor;
  List<AuditObfuscator> auditObfuscators = Collections.emptyList();

  @Test
  void shouldPopulateAuditReportInContainer() throws IOException {

    AuditClientHttpRequestInterceptor interceptor =
        new AuditClientHttpRequestInterceptor(auditorEvents, auditObfuscators, true);
    interceptor.intercept(new Request(), "A_BODY".getBytes(), new RequestExecution());

    ArgumentCaptor.forClass(HttpExchangeAudit.class);
    verify(auditorEvents, times(1)).addAuditEvents(anyString(), exchangeReportCaptor.capture());

    HttpExchangeAudit httpExchangeAudit =
        (HttpExchangeAudit) exchangeReportCaptor.getValue().get(0);
    assertThat(httpExchangeAudit.getHttpAuditedRequest().getUri()).isEqualTo("AN_URI");
    assertThat(httpExchangeAudit.getHttpAuditedRequest().getBody()).isEqualTo("A_BODY");
    assertThat(httpExchangeAudit.getHttpAuditedRequest().getHeaders().get("testHeaderName"))
        .containsExactly("testHeaderValue");
    assertThat(httpExchangeAudit.getHttpAuditedRequest().getMethod()).isEqualTo("GET");
    assertThat(httpExchangeAudit.getHttpAuditedResponse().getPayload()).isEqualTo("RESPONSE_BODY");
    assertThat(httpExchangeAudit.getHttpAuditedResponse().getHeaders()).isEmpty();
    assertThat(httpExchangeAudit.getHttpAuditedResponse().getStatus()).isEqualTo(200);
  }

  @Test
  void should_call_obfucator_for_request_and_response() throws IOException {
    RequestBodyAuditObfuscator requestBodyAuditObfuscator = mock(RequestBodyAuditObfuscator.class);
    ResponseBodyAuditObfuscator responseBodyAuditObfuscator =
        mock(ResponseBodyAuditObfuscator.class);

    List<AuditObfuscator> auditObfuscators =
        List.of(responseBodyAuditObfuscator, requestBodyAuditObfuscator);
    AuditClientHttpRequestInterceptor interceptor =
        new AuditClientHttpRequestInterceptor(auditorEvents, auditObfuscators, true);
    Request request = new Request();
    interceptor.intercept(request, "A_BODY".getBytes(), new RequestExecution());

    verify(requestBodyAuditObfuscator, times(1)).obfuscateBody(request.getURI(), "A_BODY");
    verify(responseBodyAuditObfuscator, times(1)).obfuscateBody(request.getURI(), "RESPONSE_BODY");
  }

  private static class Request implements HttpRequest {

    HttpHeaders headers = new HttpHeaders();

    @Override
    public HttpMethod getMethod() {
      return HttpMethod.GET;
    }

    @SneakyThrows
    @Override
    public URI getURI() {
      return new URI("AN_URI");
    }

    @Override
    public Map<String, Object> getAttributes() {
      return Map.of();
    }

    @Override
    public HttpHeaders getHeaders() {
      headers.add("testHeaderName", "testHeaderValue");
      return headers;
    }
  }

  private static class RequestExecution implements ClientHttpRequestExecution {

    @Override
    public ClientHttpResponse execute(HttpRequest request, byte[] body) {
      return new BufferingMockClientHttpResponse(
          "RESPONSE_BODY".getBytes(StandardCharsets.UTF_8), HttpStatus.OK);
    }
  }

  private static class BufferingMockClientHttpResponse extends MockClientHttpResponse {

    public BufferingMockClientHttpResponse(byte[] body, HttpStatus statusCode) {
      super(body, statusCode);
    }
  }
}
