package com.accor.wcp.sample.audit;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
public class SpringAsyncAuditController {

  private final RestTemplate restTemplate;
  private final SpringAsyncAuditController me;

  public SpringAsyncAuditController(
      @Lazy SpringAsyncAuditController me, RestTemplate restTemplate) {
    this.me = me;
    this.restTemplate = restTemplate;
  }

  @GetMapping(value = "/audit/async/http-exchanges")
  public ResponseEntity<String> waitForResponses() {
    String parentId = UUID.randomUUID().toString();
    List<Future<ResponseEntity<String>>> futures =
        IntStream.range(0, 10).mapToObj(i -> me.externalCall(parentId, i)).toList();
    String responses =
        futures.stream()
            .map(HttpExchangesRestTemplateAuditController::readBody)
            .collect(Collectors.joining(",", "[", "]"));
    return ResponseEntity.ok(responses);
  }

  @GetMapping(value = "/audit/async/http-exchanges-noresult")
  public ResponseEntity<String> noWait() {
    String parentId = UUID.randomUUID().toString();
    IntStream.range(0, 10).forEach(i -> me.externalCallNoWait(parentId, i));
    return ResponseEntity.ok("EMPTY");
  }

  @Async
  void externalCallNoWait(String parentId, int i) {
    String url = "https://httpbin.org/headers?parentId-" + parentId + "_index-" + i;
    log.info("Call external url: {}", url);
    ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
    log.debug("responseEntity not used in async: {}", responseEntity);
  }

  @Async
  Future<ResponseEntity<String>> externalCall(String parentId, int i) {
    String url = "https://httpbin.org/headers?parentId-" + parentId + "_index-" + i;
    log.info("Call external url: {}", url);
    return new AsyncResult<>(restTemplate.getForEntity(url, String.class));
  }
}
