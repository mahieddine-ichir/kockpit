package com.accor.wcp.console.services.featureflipping.dynamo.config;

import com.accor.wcp.console.services.featureflipping.dynamo.domain.FeatureFlippingDocument;
import com.accor.wcp.console.services.featureflipping.dynamo.domain.FeatureFlippingHistoryDocument;
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

import java.net.URI;

@Configuration
@Slf4j
@Profile({"dev", "local"})
class LocalDynamoDbConfigForFeatureFlipping {

  @Value("${aws.dynamodb.endpoint:}")
  private String dynamoDbEndpoint;

  @Value("${aws.region}")
  private String region;

  @Value("${application.dynamodb_table_name.featureflipping}")
  private String tableName;

  @Value("${application.dynamodb_table_name.featureflipping_history}")
  private String historyTableName;

  @Bean
  public DynamoDbClient amazonDynamoDBForFeatureFlipping() {
    return DynamoDbClient.builder()
        .httpClientBuilder(ApacheHttpClient.builder())
        .endpointOverride(URI.create(dynamoDbEndpoint))
        .region(Region.of(region))
        .build();
  }

  public DynamoDbEnhancedClient getDynamoDbEnhancedClientForFeatureFlipping() {
    return DynamoDbEnhancedClient.builder().dynamoDbClient(amazonDynamoDBForFeatureFlipping()).build();
  }

  @Bean
  DynamoDbTable<FeatureFlippingDocument> featureFlippingDocumentsTable() {
    return getDynamoDbEnhancedClientForFeatureFlipping().table(tableName, TableSchema.fromBean(FeatureFlippingDocument.class));
  }

  @Bean
  DynamoDbTable<FeatureFlippingHistoryDocument> featureFlippingDocumentsHistoryTable() {
    return getDynamoDbEnhancedClientForFeatureFlipping().table(historyTableName, TableSchema.fromBean(FeatureFlippingHistoryDocument.class));
  }
}
