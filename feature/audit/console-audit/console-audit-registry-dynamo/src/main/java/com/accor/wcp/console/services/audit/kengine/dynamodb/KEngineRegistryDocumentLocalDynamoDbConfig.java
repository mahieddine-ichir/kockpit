package com.accor.wcp.console.services.audit.kengine.dynamodb;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
@Slf4j
@Profile({"dev", "local"})
public class KEngineRegistryDocumentLocalDynamoDbConfig {

  @Value("${aws.dynamodb.endpoint:}")
  private String dynamoDbEndpoint;

  @Value("${aws.region:eu-west1}")
  private String region;

  @Value("${application.dynamodb_table_name.kengine}")
  private String tableName;

  @Bean
  public DynamoDbClient dynamoDbClientForAudit() {
    return DynamoDbClient.builder()
        .httpClientBuilder(ApacheHttpClient.builder())
        .endpointOverride(URI.create(dynamoDbEndpoint))
        .region(Region.of(region))
        .build();
  }

  public DynamoDbEnhancedClient getDynamoDbEnhancedClientForAudit() {
    return DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClientForAudit()).build();
  }

  @Bean
  DynamoDbTable<KEngineRegistryDocument> kEngineRegistryDocumentDynamoDbTable() {
    return getDynamoDbEnhancedClientForAudit()
        .table(tableName, TableSchema.fromBean(KEngineRegistryDocument.class));
  }
}
