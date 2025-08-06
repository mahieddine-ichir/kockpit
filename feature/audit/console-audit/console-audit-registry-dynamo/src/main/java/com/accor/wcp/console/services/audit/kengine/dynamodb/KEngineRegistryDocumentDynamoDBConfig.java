package com.accor.wcp.console.services.audit.kengine.dynamodb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
@Slf4j
@Profile("aws")
public class KEngineRegistryDocumentDynamoDBConfig {

  @Value("${application.dynamodb_table_name.kengine}")
  private String tableName;

  @Bean
  public DynamoDbClient amazonDynamoDBForAudit() {
    return DynamoDbClient.builder()
        .httpClientBuilder(ApacheHttpClient.builder())
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();
  }

  public DynamoDbEnhancedClient getDynamoDbEnhancedClientForAudit() {
    return DynamoDbEnhancedClient.builder().dynamoDbClient(amazonDynamoDBForAudit()).build();
  }

  @Bean
  DynamoDbTable<KEngineRegistryDocument> kEngineRegistryDocumentDynamoDbTable() {
    return getDynamoDbEnhancedClientForAudit()
        .table(tableName, TableSchema.fromBean(KEngineRegistryDocument.class));
  }
}
