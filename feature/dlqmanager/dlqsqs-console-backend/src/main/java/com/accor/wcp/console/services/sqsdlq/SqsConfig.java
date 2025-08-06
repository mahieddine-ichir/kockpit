package com.accor.wcp.console.services.sqsdlq;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
class SqsConfig {

  @Bean("amazonSQS")
  @Profile("aws")
  public SqsClient getSQSClient() {
    return SqsClient.builder()
        .httpClientBuilder(ApacheHttpClient.builder())
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();

  }

  @Bean("amazonSQS")
  @Profile({"dev", "local"})
  public SqsClient getSQSClientLocal(
      @Value("${aws.sqs.endpoint}") String endpoint, @Value("${aws.region}") String region) {

    return SqsClient.builder()
        .httpClientBuilder(ApacheHttpClient.builder())
        .endpointOverride(URI.create(endpoint))
        .region(Region.of(region))
        .build();
  }
}
