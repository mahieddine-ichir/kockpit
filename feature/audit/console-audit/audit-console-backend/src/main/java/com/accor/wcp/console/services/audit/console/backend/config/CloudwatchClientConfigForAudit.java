package com.accor.wcp.console.services.audit.console.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Configuration
@Slf4j
@Profile("aws")
class CloudwatchClientConfigForAudit {

  @Bean
  CloudWatchClient amazonCloudwatchForAuditService() {
    return CloudWatchClient.builder()
        .httpClientBuilder(ApacheHttpClient.builder())
        .credentialsProvider(DefaultCredentialsProvider.create())
        .region(Region.EU_WEST_1)
        .build();
  }
}
