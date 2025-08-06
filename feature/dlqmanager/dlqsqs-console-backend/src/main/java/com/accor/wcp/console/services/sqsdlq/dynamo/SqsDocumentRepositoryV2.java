package com.accor.wcp.console.services.sqsdlq.dynamo;

import com.accor.wcp.console.services.sqsdlq.config.ApplicationProperties;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Repository
public class SqsDocumentRepositoryV2 extends DynamoDbRepository<SqsDocumentV2> {

  public SqsDocumentRepositoryV2(
      ApplicationProperties applicationProperties,
      DynamoDbClient dynamoDbClientForSqsDlq,
      DynamoDbEnhancedClient dynamoDbEnhancedClientForSqsDlq,
      DynamoDbTable<SqsDocumentV2> dynamoDbTableForSqsDlqV2,
      @Value("${sqsdlq.deleter.threads:5}") Integer nbThreadsForDeleteBatch) {
    super(
        applicationProperties,
        dynamoDbClientForSqsDlq,
        dynamoDbEnhancedClientForSqsDlq,
        dynamoDbTableForSqsDlqV2,
        SqsDocumentV2.class,
        nbThreadsForDeleteBatch);
  }
}
