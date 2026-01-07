package org.kockpit.ai.mcp.server;

import lombok.SneakyThrows;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequestInterceptor;
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
    HttpHost[] hosts = Stream.of(endpoints.split(","))
            .map(String::trim)
            .map(this::create)
            .toArray(HttpHost[]::new);

    RestClientBuilder restClientBuilder = RestClient.builder(hosts);
    if (!CollectionUtils.isEmpty(requestInterceptors)) {
      requestInterceptors.forEach(httpRequestInterceptor -> restClientBuilder.setHttpClientConfigCallback(httpClientBuilder ->
              httpClientBuilder.addRequestInterceptorLast(httpRequestInterceptor))
      );
    }
    return new RestHighLevelClient(restClientBuilder);
  }

  @SneakyThrows
  private HttpHost create(String endpoint) {
    return HttpHost.create(endpoint);
  }
}
