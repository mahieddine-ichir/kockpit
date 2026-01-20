package org.kockpit.ai.mcp.server;

import lombok.SneakyThrows;
import org.apache.hc.client5.http.async.AsyncExecChainHandler;
import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Stream;

@Configuration
class RestClientConfig {

  @Bean
  RestHighLevelClient searchClient(
          @Value("${opensearch.endpoints}") String endpoints,
          List<AsyncExecChainHandler> handlers
  ) {
    System.out.printf("""
      Opensearch endpoints: %s
    %n""", endpoints);
    System.out.println("RestClientConfig - Number of HttpRequestInterceptors: " + (handlers != null ? handlers.size() : "null"));

    HttpHost[] hosts = Stream.of(endpoints.split(","))
            .map(String::trim)
            .map(this::create)
            .toArray(HttpHost[]::new);

    RestClientBuilder restClientBuilder = RestClient.builder(hosts);

    // Configure HTTP client with interceptors for AWS signing
    if (!CollectionUtils.isEmpty(handlers)) {
      System.out.println("RestClientConfig - Registering " + handlers.size() + " handlers");
      restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
        for (AsyncExecChainHandler handler : handlers) {
          System.out.println("RestClientConfig - Adding interceptor: " + handler.getClass().getName());
          httpClientBuilder.addExecInterceptorLast(handler.getClass().getName(), handler);
        }
        return httpClientBuilder;
      });
    } else {
      System.out.println("RestClientConfig - No interceptors to register");
    }

    return new RestHighLevelClient(restClientBuilder);
  }

  @SneakyThrows
  private HttpHost create(String endpoint) {
    return HttpHost.create(endpoint);
  }
}
