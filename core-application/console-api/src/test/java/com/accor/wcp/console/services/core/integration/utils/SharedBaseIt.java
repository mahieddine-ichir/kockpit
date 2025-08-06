package com.accor.wcp.console.services.core.integration.utils;

import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * Force all class test, to use the same : - LocalStackContainer - SqsClient - S3Client - Spring
 * context
 */
public class SharedBaseIt extends BaseIt {

  private static boolean initialized;
  private static LocalStackContainer localStackContainer;
  protected static DynamoDbTable<SqsDocumentV2> dynamoDbTable;
  protected static SqsClient sqsClient;
  protected static S3Client s3Client;

  @BeforeAll
  static void beforeAll() {
    System.setProperty("aws.accessKeyId", "test");
    System.setProperty("aws.secretAccessKey", "test");
    System.setProperty("aws.region", "eu-west-1");

    if (!initialized) {
      localStackContainer = initLocalStackContainer();
      dynamoDbTable =
          initDynamoDB(
              localStackContainer.getEndpointOverride(LocalStackContainer.Service.DYNAMODB));
      sqsClient =
          initSqsClient(localStackContainer.getEndpointOverride(LocalStackContainer.Service.SQS));
      s3Client =
          initS3Client(localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3));
      initialized = true;
    }
  }

  @DynamicPropertySource
  public static void properties(DynamicPropertyRegistry registry) {
    setSpringProperties(registry, localStackContainer);
  }
}
