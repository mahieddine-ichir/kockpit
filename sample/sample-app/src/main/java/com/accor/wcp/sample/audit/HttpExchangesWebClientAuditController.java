package com.accor.wcp.sample.audit;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class HttpExchangesWebClientAuditController {

  private final WebClient webClient;

  public HttpExchangesWebClientAuditController(WebClient webClient) {
    this.webClient = webClient;
  }

  @GetMapping(value = "/audit/multithread/http-exchanges/webclient")
  public String home() {
    String parentId = UUID.randomUUID().toString();
    List<Mono<String>> monoList =
        IntStream.range(0, 10).mapToObj(i -> this.externalCall(parentId, i)).toList();
    return monoList.stream()
        .map(HttpExchangesWebClientAuditController::readBody)
        .collect(Collectors.joining(",", "[", "]"));
  }

  static String readBody(Mono<String> responseEntityFuture) {
    return responseEntityFuture.block();
  }

  private Mono<String> externalCall(String parentId, int i) {
    String url = "https://httpbin.org/headers?parentId-" + parentId + "_index-" + i;
    log.info("Call external url: {}", url);
    return webClient.get().uri(url).retrieve().bodyToMono(String.class);
  }
}
