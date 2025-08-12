package com.accor.wcp.services.auditstream.config;

import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("local")
@Configuration
public class RestClientConfigForLocal {

  @Value("${opensearch.endpoint}")
  private String endpoint;

  @Bean
  public RestHighLevelClient opensearchClient() {
    return new RestHighLevelClient(RestClient.builder(HttpHost.create(endpoint)));
  }
}
