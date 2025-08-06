package com.accor.wcp.console.services.audit.console.backend.config;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Configuration
@Slf4j
@Profile({"dev", "local"})
class LocalCloudwatchClientConfigForAudit {

  @Value("${aws.cloudwatch.endpoint:}")
  private String cloudwatchEndpoint;

  @Value("${aws.region}")
  private String region;

  @Bean
  public CloudWatchClient amazonCloudwatchForAuditService() {
    return CloudWatchClient.builder()
        .httpClientBuilder(ApacheHttpClient.builder())
        .endpointOverride(URI.create(cloudwatchEndpoint))
        .region(Region.of(region))
        .build();
  }
}
