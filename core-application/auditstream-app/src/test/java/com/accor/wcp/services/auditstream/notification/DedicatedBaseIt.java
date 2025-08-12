package com.accor.wcp.services.auditstream.notification;

import com.accor.wcp.console.services.audit.kengine.dynamodb.KEngineRegistryDocument;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.opensearch.testcontainers.OpensearchContainer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;

/**
 * Force all class test, to use a new : - ElasticsearchContainer - LocalStackContainer -
 * KinesisAsyncClient - Spring context
 */
@Slf4j
@DirtiesContext
public class DedicatedBaseIt extends BaseIt {
  private static OpensearchContainer opensearchContainer;
  private static LocalStackContainer localStackContainer;
  protected static KinesisAsyncClient kinesisAsyncClient;
  protected static DynamoDbTable<KEngineRegistryDocument> dynamoDbTable;

  @BeforeAll
  static void beforeAll() throws Exception {
    opensearchContainer = initOpensearchContainer();
    localStackContainer = initLocalStackContainer();
    kinesisAsyncClient =
        initKinesis(localStackContainer.getEndpointOverride(LocalStackContainer.Service.KINESIS));
    dynamoDbTable =
        initDynamoDB(localStackContainer.getEndpointOverride(LocalStackContainer.Service.DYNAMODB));
  }

  @AfterAll
  static void afterAll() {
    log.info(
        "Stopping containers {} and {} ",
        opensearchContainer.getContainerInfo().getName(),
        localStackContainer.getContainerInfo().getName());
    dynamoDbTable.deleteTable();
    opensearchContainer.stop();
    localStackContainer.stop();
    kinesisAsyncClient.close();
  }

  @DynamicPropertySource
  public static void properties(DynamicPropertyRegistry registry) {
    setSpringProperties(registry, opensearchContainer, localStackContainer);
  }
}
