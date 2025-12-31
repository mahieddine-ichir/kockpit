package org.kockpit.audit.httpexchange;

import org.kockpit.audit.api.AuditNotStartedException;
import org.kockpit.audit.api.AuditorEventService;
import org.kockpit.audit.httpexchange.obfuscator.AuditObfuscator;
import org.kockpit.audit.httpexchange.obfuscator.RequestBodyAuditObfuscator;
import org.kockpit.audit.httpexchange.obfuscator.ResponseBodyAuditObfuscator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.kockpit.audit.httpexchange.HttpExchangeAudit.logHttpExchange;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@AllArgsConstructor
public class AuditClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

  public static final String RESTTEMPLATE_MISSING_BUFFERING_CLIENT_HTTP_REQUEST_FACTORY =
      "RestTemplate was not configured to use BufferingClientHttpRequestFactory";
  private final AuditorEventService auditorEvents;
  private final List<AuditObfuscator> auditedBodyObfuscators;

  private final boolean filterAuthorizationHeader;

  @Override
  public ClientHttpResponse intercept(
      HttpRequest httpRequest, byte[] body, ClientHttpRequestExecution clientHttpRequestExecution)
      throws IOException {
    long startTime = System.currentTimeMillis();
    try {
      ClientHttpResponse response = clientHttpRequestExecution.execute(httpRequest, body);
      HttpAuditedResponse httpAuditedResponse =
          buildHttpAuditResponse(httpRequest.getURI(), response);
      HttpAuditedRequest httpAuditedRequest = buildHttpAuditRequest(httpRequest, body);
      storeHttpExchangeModuleAuditReport(startTime, httpAuditedRequest, httpAuditedResponse);
      return response;
    } catch (Exception e) {
      HttpAuditedResponse httpAuditedResponse = buildErrorHttpAuditResponse(e.getMessage());
      HttpAuditedRequest httpAuditedRequest = buildHttpAuditRequest(httpRequest, body);
      storeHttpExchangeModuleAuditReport(startTime, httpAuditedRequest, httpAuditedResponse);
      throw e;
    }
  }

  private void storeHttpExchangeModuleAuditReport(
      long startTime,
      HttpAuditedRequest httpAuditedRequest,
      HttpAuditedResponse httpAuditedResponse) {
    try {
      HttpExchangeAudit httpExchangeAudit =
          HttpExchangeAudit.builder()
              .startTime(startTime)
              .endTime(System.currentTimeMillis())
              .httpAuditedRequest(httpAuditedRequest)
              .httpAuditedResponse(httpAuditedResponse)
              .build();
      logHttpExchange(httpExchangeAudit);
      auditorEvents.addAuditEvents(HttpExchangeAudit.TYPE, List.of(httpExchangeAudit));
    } catch (AuditNotStartedException anse) {
      log.info("Audit not started, ignoring this http call");
    }
  }

  private HttpAuditedResponse buildHttpAuditResponse(URI uri, ClientHttpResponse response)
      throws IOException {
    return HttpAuditedResponse.builder()
        .status(response.getStatusCode().value())
        .headers(getHttpHeaders(response.getHeaders()))
        .payload(getPayload(uri, response))
        .build();
  }

  private String getPayload(URI uri, ClientHttpResponse response) throws IOException {
    boolean isResponseReadable = response.getClass().getName().contains("Buffering");
    String body =
        isResponseReadable
            ? StreamUtils.copyToString(response.getBody(), Charset.defaultCharset())
            : RESTTEMPLATE_MISSING_BUFFERING_CLIENT_HTTP_REQUEST_FACTORY;
    return obfuscateResponseBody(uri, body);
  }

  private HttpAuditedResponse buildErrorHttpAuditResponse(String message) {
    return HttpAuditedResponse.builder().status(0).headers(null).payload(message).build();
  }

  private HttpAuditedRequest buildHttpAuditRequest(HttpRequest request, byte[] body) {
    // Extract query params
    Map<String, List<String>> params = null;
    String query = request.getURI().getQuery();
    if (nonNull(query)) {
      params =
          UriComponentsBuilder.fromUriString(request.getURI().toString()).build().getQueryParams();
    }

    return HttpAuditedRequest.builder()
        .headers(getHttpHeaders(request.getHeaders()))
        .uri(request.getURI().toString())
        .method(request.getMethod().name())
        .body(obfuscateRequestBody(request.getURI(), new String(body, StandardCharsets.UTF_8)))
        .params(getParameters(params))
        .build();
  }

  private Map<String, List<String>> getParameters(Map<String, List<String>> params) {
    if (isNull(params)) {
      return null;
    }
    return new HashMap<>(params);
  }

  private String obfuscateRequestBody(URI uri, String body) {
    List<AuditObfuscator> requestsObfuscator =
        Optional.ofNullable(auditedBodyObfuscators).stream()
            .flatMap(Collection::stream)
            .filter(RequestBodyAuditObfuscator.class::isInstance)
            .toList();

    for (AuditObfuscator auditObfuscator : requestsObfuscator) {
      body = auditObfuscator.obfuscateBody(uri, body);
    }
    return body;
  }

  private String obfuscateResponseBody(URI uri, String body) {
    List<AuditObfuscator> responsesObfuscator =
        Optional.ofNullable(auditedBodyObfuscators).stream()
            .flatMap(Collection::stream)
            .filter(ResponseBodyAuditObfuscator.class::isInstance)
            .toList();

    for (AuditObfuscator responseAuditObfuscator : responsesObfuscator) {
      body = responseAuditObfuscator.obfuscateBody(uri, body);
    }
    return body;
  }

  private HttpHeaders getHttpHeaders(HttpHeaders httpHeaders) {
    HttpHeaders headersWithoutAuthorization = new HttpHeaders();
    httpHeaders.entrySet().stream()
        .filter(
            header ->
                !filterAuthorizationHeader || !AUTHORIZATION.equalsIgnoreCase(header.getKey()))
        .forEach(header -> headersWithoutAuthorization.addAll(header.getKey(), header.getValue()));
    return headersWithoutAuthorization;
  }
}
