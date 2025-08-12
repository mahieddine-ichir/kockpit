package com.accor.wcp.services.auditstream.config;

import io.github.acm19.aws.interceptor.http.AwsRequestSigningApacheInterceptor;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;

@Profile("aws")
@Configuration
public class RestClientConfig {

  @Value("${opensearch.endpoint}")
  private String endpoint;

  @Value("${opensearch.aws.service_name:es}")
  private String serviceName;

  @Value("${opensearch.aws.region:eu-west-1}")
  private String region;

  @Bean
  public RestHighLevelClient opensearchClient() {
    return searchClient(serviceName, region);
  }

  // Adds AWS Request signing interceptor to the ES REST client
  public RestHighLevelClient searchClient(String serviceName, String region) {
    DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
    Aws4Signer signer = Aws4Signer.create();
    HttpRequestInterceptor interceptor =
        new AwsRequestSigningApacheInterceptor(serviceName, signer, credentialsProvider, region);

    return new RestHighLevelClient(
        RestClient.builder(HttpHost.create(endpoint))
            .setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
  }
}
