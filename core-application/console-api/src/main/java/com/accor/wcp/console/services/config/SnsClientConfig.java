package com.accor.wcp.console.services.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class SnsClientConfig {

  @Value("${aws.region}")
  private String region;

  @Value("${aws.sns.endpoint:}")
  private String snsEndpoint;

  @Bean
  @Profile({"aws"})
  public SnsClient snsClientAws() {
    return SnsClient.builder()
        .httpClientBuilder(ApacheHttpClient.builder())
        .region(Region.of(region))
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();
  }

  @Bean
  @Profile({"dev", "local"})
  public SnsClient snsClientLocal() {
    return SnsClient.builder()
        .region(Region.of(region))
        .httpClientBuilder(ApacheHttpClient.builder())
        .credentialsProvider(DefaultCredentialsProvider.create())
        .endpointOverride(URI.create(snsEndpoint))
        .build();
  }
}
