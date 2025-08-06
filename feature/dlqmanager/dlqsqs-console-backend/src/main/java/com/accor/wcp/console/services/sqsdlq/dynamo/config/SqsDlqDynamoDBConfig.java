package com.accor.wcp.console.services.sqsdlq.dynamo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
@Slf4j
@Profile("aws")
public class SqsDlqDynamoDBConfig extends AbstractSqsDlqDynamoDBConfig {

  @Bean
  public DynamoDbClient dynamoDbClientForSqsDlq() {
    return DynamoDbClient.builder()
        .httpClientBuilder(ApacheHttpClient.builder())
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();
  }
}
