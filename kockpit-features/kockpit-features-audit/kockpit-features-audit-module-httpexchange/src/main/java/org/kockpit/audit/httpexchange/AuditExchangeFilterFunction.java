package org.kockpit.audit.httpexchange;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditNotStartedException;
import org.kockpit.audit.api.AuditReport;
import org.kockpit.audit.api.AuditorEventService;
import org.kockpit.audit.api.AuditorService;
import org.kockpit.audit.httpexchange.obfuscator.AuditObfuscator;
import org.kockpit.audit.httpexchange.obfuscator.ResponseBodyAuditObfuscator;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.kockpit.audit.httpexchange.HttpExchangeAudit.logHttpExchange;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@AllArgsConstructor
public class AuditExchangeFilterFunction implements ExchangeFilterFunction {

  private final AuditorEventService auditorEvents;
  private final AuditorService auditorService;
  private final List<AuditObfuscator> auditedBodyObfuscators;
  private final boolean filterAuthorizationHeader;

  @Override
  public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
    long startTime = System.currentTimeMillis();
    HttpAuditedRequest auditedRequest = buildHttpAuditRequest(request);
    // Capture the audit report on the calling thread (servlet thread has the ThreadLocal set).
    // The flatMap/onErrorResume callbacks run on a Netty I/O thread where the ThreadLocal is empty,
    // so we must carry the reference explicitly and restore it before calling storeAudit.
    AuditReport capturedReport = auditorService.currentAuditReport();

    return next.exchange(request)
        .flatMap(
            response ->
                response
                    .bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .map(
                        body -> {
                          HttpAuditedResponse auditedResponse =
                              buildHttpAuditResponse(request.url(), response, body);
                          auditorService.executeWithAuditReport(
                              capturedReport,
                              () -> storeAudit(startTime, auditedRequest, auditedResponse));
                          return ClientResponse.create(response.statusCode())
                              .headers(h -> h.addAll(response.headers().asHttpHeaders()))
                              .body(body)
                              .build();
                        }))
        .onErrorResume(
            e -> {
              HttpAuditedResponse errorResponse =
                  HttpAuditedResponse.builder().status(0).headers(null).payload(e.getMessage()).build();
              auditorService.executeWithAuditReport(
                  capturedReport, () -> storeAudit(startTime, auditedRequest, errorResponse));
              return Mono.error(e);
            });
  }

  private HttpAuditedRequest buildHttpAuditRequest(ClientRequest request) {
    URI uri = request.url();
    var params = UriComponentsBuilder.fromUri(uri).build().getQueryParams();

    return HttpAuditedRequest.builder()
        .uri(uri.toString())
        .method(request.method().name())
        .headers(filterHeaders(request.headers()))
        .body(null)
        .params(params.isEmpty() ? null : params)
        .build();
  }

  private HttpAuditedResponse buildHttpAuditResponse(
      URI uri, ClientResponse response, String body) {
    return HttpAuditedResponse.builder()
        .status(response.statusCode().value())
        .headers(filterHeaders(response.headers().asHttpHeaders()))
        .payload(obfuscateResponseBody(uri, body))
        .build();
  }

  private void storeAudit(
      long startTime, HttpAuditedRequest auditedRequest, HttpAuditedResponse auditedResponse) {
    try {
      HttpExchangeAudit audit =
          HttpExchangeAudit.builder()
              .startTime(startTime)
              .endTime(System.currentTimeMillis())
              .httpAuditedRequest(auditedRequest)
              .httpAuditedResponse(auditedResponse)
              .build();
      logHttpExchange(audit);
      auditorEvents.addAuditEvents(HttpExchangeAudit.TYPE, List.of(audit));
    } catch (AuditNotStartedException e) {
      log.info("Audit not started, ignoring this http call");
    }
  }

  private String obfuscateResponseBody(URI uri, String body) {
    List<AuditObfuscator> responseObfuscators =
        Optional.ofNullable(auditedBodyObfuscators).stream()
            .flatMap(Collection::stream)
            .filter(ResponseBodyAuditObfuscator.class::isInstance)
            .toList();
    for (AuditObfuscator obfuscator : responseObfuscators) {
      body = obfuscator.obfuscateBody(uri, body);
    }
    return body;
  }

  private HttpHeaders filterHeaders(HttpHeaders headers) {
    HttpHeaders filtered = new HttpHeaders();
    headers.headerSet().stream()
        .filter(
            entry ->
                !filterAuthorizationHeader || !AUTHORIZATION.equalsIgnoreCase(entry.getKey()))
        .forEach(entry -> filtered.addAll(entry.getKey(), entry.getValue()));
    return filtered;
  }
}
