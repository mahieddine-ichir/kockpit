package com.accor.wcp.console.services.sqsdlq.dynamo.config;

import com.accor.wcp.console.services.sqsdlq.config.ApplicationProperties;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocument;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public abstract class AbstractSqsDlqDynamoDBConfig {

  @Bean
  public DynamoDbEnhancedClient dynamoDbEnhancedClientForSqsDlq(
      DynamoDbClient dynamoDbClientForSqsDlq) {
    return DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClientForSqsDlq).build();
  }

  @Bean
  public DynamoDbTable<SqsDocument> sqsDocumentsTable(
      DynamoDbEnhancedClient dynamoDbEnhancedClientForSqsDlq, ApplicationProperties properties) {
    return dynamoDbEnhancedClientForSqsDlq.table(
        properties.getTableName(), TableSchema.fromBean(SqsDocument.class));
  }

  @Bean
  public DynamoDbTable<SqsDocumentV2> dynamoDbTableForSqsDlqV2(
      DynamoDbEnhancedClient dynamoDbEnhancedClientForSqsDlq, ApplicationProperties properties) {
    return dynamoDbEnhancedClientForSqsDlq.table(
        properties.getTableNameV2(), TableSchema.fromBean(SqsDocumentV2.class));
  }
}
