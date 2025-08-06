package com.accor.wcp.console.services.sqsdlq.dynamo.config;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
@Slf4j
@Profile({"dev", "local"})
public class SqsDlqDynamoDBConfigLocal extends AbstractSqsDlqDynamoDBConfig {
  @Value("${aws.dynamodb.endpoint:}")
  private String dynamoDbEndpoint;

  @Value("${aws.region}")
  private String region;

  @Bean
  public DynamoDbClient dynamoDbClientForSqsDlq() {
    return DynamoDbClient.builder()
        .httpClientBuilder(ApacheHttpClient.builder())
        .endpointOverride(URI.create(dynamoDbEndpoint))
        .region(Region.of(region))
        .build();
  }
}
