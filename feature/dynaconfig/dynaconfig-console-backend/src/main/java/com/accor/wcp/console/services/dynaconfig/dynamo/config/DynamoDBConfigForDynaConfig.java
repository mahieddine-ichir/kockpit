package com.accor.wcp.console.services.dynaconfig.dynamo.config;

import com.accor.wcp.console.services.dynaconfig.dynamo.domain.DynaConfigDocument;
import com.accor.wcp.console.services.dynaconfig.dynamo.domain.DynaConfigHistoryDocument;
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
public class DynamoDBConfigForDynaConfig {

  @Value("${application.dynamodb_table_name.dynaconfig}")
  private String tableName;

  @Value("${application.dynamodb_table_name.dynaconfig_history}")
  private String historyTableName;

  @Bean
  public DynamoDbClient amazonDynamoDBForDynaConfig() {
    return DynamoDbClient.builder()
        .httpClientBuilder(ApacheHttpClient.builder())
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();
  }

  public DynamoDbEnhancedClient getDynamoDbEnhancedClientForDynaConfig() {
    return DynamoDbEnhancedClient.builder().dynamoDbClient(amazonDynamoDBForDynaConfig()).build();
  }

  @Bean
  DynamoDbTable<DynaConfigDocument> dynaConfigDocumentsTable() {
    return getDynamoDbEnhancedClientForDynaConfig().table(tableName, TableSchema.fromBean(DynaConfigDocument.class));
  }

  @Bean
  DynamoDbTable<DynaConfigHistoryDocument> dynaConfigDocumentsHistoryTable() {
    return getDynamoDbEnhancedClientForDynaConfig().table(historyTableName, TableSchema.fromBean(DynaConfigHistoryDocument.class));
  }

}
