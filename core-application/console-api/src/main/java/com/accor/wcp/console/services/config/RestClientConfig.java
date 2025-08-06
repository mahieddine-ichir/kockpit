package com.accor.wcp.console.services.config;

import io.github.acm19.aws.interceptor.http.AwsRequestSigningApacheInterceptor;
import java.time.Duration;
import lombok.Data;
import lombok.Setter;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;

@Profile("aws")
@Configuration
@ConfigurationProperties(prefix = "search")
class RestClientConfig {

  @Setter private String opensearchEndpoint;

  @Setter private AwsConfig aws;

  @Setter private Duration connectionTimeout = Duration.ofSeconds(5L);
  @Setter private Duration socketTimeout = Duration.ofSeconds(120L);

  @Bean
  @Primary
  public RestHighLevelClient opensearchClient() {
    return searchClient(aws.serviceName, aws.region, opensearchEndpoint);
  }

  // Adds AWS Request signing interceptor to the ES REST client
  private RestHighLevelClient searchClient(String serviceName, String region, String endpoint) {
    DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
    Aws4Signer signer = Aws4Signer.create();
    HttpRequestInterceptor interceptor =
        new AwsRequestSigningApacheInterceptor(serviceName, signer, credentialsProvider, region);

    return new RestHighLevelClient(
        RestClient.builder(HttpHost.create(endpoint))
            .setHttpClientConfigCallback(
                httpAsyncClientBuilder -> httpAsyncClientBuilder.addInterceptorLast(interceptor))
            .setRequestConfigCallback(
                requestConfigBuilder ->
                    requestConfigBuilder
                        .setConnectTimeout((int) connectionTimeout.toMillis())
                        .setSocketTimeout((int) socketTimeout.toMillis())));
  }

  @Data
  public static class AwsConfig {

    private String serviceName;

    private String region;
  }
}
