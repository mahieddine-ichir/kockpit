package com.accor.wcp.sample.audit;

import com.accor.wcp.audit.AuditedDelegateExecutorService;
import io.opentelemetry.context.Context;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
public class HttpExchangesRestTemplateAuditController {

  private final RestTemplate restTemplate;
  private final ExecutorService executorService;

  public HttpExchangesRestTemplateAuditController(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
    executorService =
        Context.taskWrapping(new AuditedDelegateExecutorService(Executors.newFixedThreadPool(8)));
  }

  @GetMapping(value = "/audit/multithread/http-exchanges")
  public ResponseEntity<String> home() {
    String parentId = UUID.randomUUID().toString();
    List<Future<ResponseEntity<String>>> futures =
        IntStream.range(0, 10).mapToObj(i -> this.externalCall(parentId, i)).toList();
    String responses =
        futures.stream()
            .map(HttpExchangesRestTemplateAuditController::readBody)
            .collect(Collectors.joining(",", "[", "]"));
    return ResponseEntity.ok(responses);
  }

  static String readBody(Future<ResponseEntity<String>> responseEntityFuture) {
    try {
      return responseEntityFuture.get().getBody();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private Future<ResponseEntity<String>> externalCall(String parentId, int i) {
    return executorService.submit(
        () -> {
          String url = "https://httpbin.org/headers?parentId-" + parentId + "_index-" + i;
          log.info("Call external url: {}", url);
          return restTemplate.getForEntity(url, String.class);
        });
  }
}
