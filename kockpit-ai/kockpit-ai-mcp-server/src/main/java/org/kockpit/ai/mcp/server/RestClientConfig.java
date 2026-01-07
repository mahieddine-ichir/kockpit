package org.kockpit.ai.mcp.server;

import lombok.SneakyThrows;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
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
          List<HttpRequestInterceptor> requestInterceptors
  ) {
    System.out.printf("""
      Opensearch endpoints: %s
    %n""", endpoints);
    System.out.println("RestClientConfig - Number of HttpRequestInterceptors: " + (requestInterceptors != null ? requestInterceptors.size() : "null"));

    HttpHost[] hosts = Stream.of(endpoints.split(","))
            .map(String::trim)
            .map(this::create)
            .toArray(HttpHost[]::new);

    RestClientBuilder restClientBuilder = RestClient.builder(hosts);

    // Configure HTTP client with interceptors for AWS signing
    if (!CollectionUtils.isEmpty(requestInterceptors)) {
      System.out.println("RestClientConfig - Registering " + requestInterceptors.size() + " interceptors");
      restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
        for (HttpRequestInterceptor interceptor : requestInterceptors) {
          System.out.println("RestClientConfig - Adding interceptor: " + interceptor.getClass().getName());
          httpClientBuilder.addInterceptorLast(interceptor);
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
