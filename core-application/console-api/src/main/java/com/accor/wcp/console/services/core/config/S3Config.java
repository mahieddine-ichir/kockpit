package com.accor.wcp.console.services.core.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
class S3Config {
  private final String endpoint;

  public S3Config(@Value("${aws.s3.endpoint:}") String endpoint) {
    this.endpoint = endpoint;
  }

  @Bean
  @Profile("aws")
  public S3Client amazonS3() {
    Region region = Region.EU_WEST_1;
    return S3Client.builder()
        .region(region)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .httpClientBuilder(ApacheHttpClient.builder())
        .build();
  }

  @Bean
  @Profile({"dev", "local"})
  public S3Client amazonLocalS3() {
    Region region = Region.EU_WEST_1;
    return S3Client.builder()
        .region(region)
        .forcePathStyle(true)
        .endpointOverride(URI.create(endpoint))
        .httpClientBuilder(ApacheHttpClient.builder())
        .build();
  }
}
