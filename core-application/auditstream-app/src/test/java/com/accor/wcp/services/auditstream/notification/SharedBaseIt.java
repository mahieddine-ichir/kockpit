package com.accor.wcp.services.auditstream.notification;

import com.accor.wcp.console.services.audit.kengine.dynamodb.KEngineRegistryDocument;
import org.junit.jupiter.api.BeforeAll;
import org.opensearch.testcontainers.OpensearchContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;

/**
 * Force all class test, to use the same : - ElasticsearchContainer - LocalStackContainer -
 * KinesisAsyncClient - Spring context
 */
public class SharedBaseIt extends BaseIt {

  private static boolean initialized;
  private static OpensearchContainer opensearchContainer;
  private static LocalStackContainer localStackContainer;
  protected static KinesisAsyncClient kinesisAsyncClient;
  protected static DynamoDbTable<KEngineRegistryDocument> dynamoDbTable;

  @BeforeAll
  static void beforeAll() throws Exception {
    System.setProperty("aws.accessKeyId", "test");
    System.setProperty("aws.secretAccessKey", "test");
    System.setProperty("aws.region", "eu-west-1");

    if (!initialized) {
      opensearchContainer = initOpensearchContainer();
      localStackContainer = initLocalStackContainer();
      kinesisAsyncClient =
          initKinesis(localStackContainer.getEndpointOverride(LocalStackContainer.Service.KINESIS));
      dynamoDbTable =
          initDynamoDB(
              localStackContainer.getEndpointOverride(LocalStackContainer.Service.DYNAMODB));
      initialized = true;
    }
  }

  @DynamicPropertySource
  public static void properties(DynamicPropertyRegistry registry) {
    setSpringProperties(registry, opensearchContainer, localStackContainer);
  }
}
