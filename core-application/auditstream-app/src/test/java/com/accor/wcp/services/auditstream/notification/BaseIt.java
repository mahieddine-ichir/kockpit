package com.accor.wcp.services.auditstream.notification;

import com.accor.wcp.console.services.audit.kengine.dynamodb.KEngineRegistryDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.testcontainers.OpensearchContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.CreateStreamRequest;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamRequest;
import software.amazon.awssdk.services.kinesis.model.ResourceInUseException;

import java.net.URI;

@Slf4j
@SpringBootTest(properties = {
        "darkcanarytesting.console-services.manifest.enabled=false"
})
@ActiveProfiles({"local", "test", "opensearch"})
public class BaseIt {

  @Autowired ObjectMapper objectMapper;

  private static final String LOCALSTACK_IMAGE = "localstack/localstack:3.5.0";
  private static final DockerImageName OPEN_SEARCH_DOCKER_IMAGE =
      DockerImageName.parse("opensearchproject/opensearch:2.0.0");
  private static final int OPENSEARCH_PORT = 9200;
  private static final int EDGE_PORT = 4566;
  private static final String AUDIT_STREAM_NAME = "audit-it";

  static OpensearchContainer initOpensearchContainer() {
    OpensearchContainer opensearchContainer =
        new OpensearchContainer(OPEN_SEARCH_DOCKER_IMAGE)
            .withExposedPorts(OPENSEARCH_PORT)
            .withEnv("discovery.type", "single-node")
            .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");
    opensearchContainer.start();
    log.info(
        "OpensearchContainer started, name : {}", opensearchContainer.getContainerInfo().getName());
    return opensearchContainer;
  }

  static LocalStackContainer initLocalStackContainer() {
    LocalStackContainer localStackContainer =
        new LocalStackContainer(DockerImageName.parse(LOCALSTACK_IMAGE))
            .withServices(
                LocalStackContainer.Service.DYNAMODB,
                LocalStackContainer.Service.KINESIS,
                LocalStackContainer.Service.CLOUDWATCH)
            .withExposedPorts(EDGE_PORT)
            .withEnv("EDGE_PORT", String.valueOf(EDGE_PORT));
    localStackContainer.start();
    log.info(
        "LocalStackContainer started, name : {}", localStackContainer.getContainerInfo().getName());
    return localStackContainer;
  }

  static DynamoDbTable<KEngineRegistryDocument> initDynamoDB(URI endpoint) {
    DynamoDbClient dynamoDbClient =
        DynamoDbClient.builder()
            .httpClientBuilder(ApacheHttpClient.builder())
            .endpointOverride(endpoint)
            .region(Region.EU_WEST_1)
            .build();

    DynamoDbEnhancedClient dynamoDbEnhancedClient =
        DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();

    DynamoDbTable<KEngineRegistryDocument> kEngineRegistryDocument =
        dynamoDbEnhancedClient.table(
            "KEngineRegistryDocument", TableSchema.fromBean(KEngineRegistryDocument.class));

    CreateTableEnhancedRequest request = CreateTableEnhancedRequest.builder().build();
    kEngineRegistryDocument.createTable(request);
    return kEngineRegistryDocument;
  }

  static KinesisAsyncClient initKinesis(URI endpointKinesis) throws Exception {

    log.info("Create kinesis client with URI : {}", endpointKinesis.toString());
    KinesisAsyncClient kinesisAsyncClient =
        KinesisAsyncClient.builder()
            .endpointOverride(endpointKinesis).build();

    log.info("Create kinesis stream : {} ", AUDIT_STREAM_NAME);
    try {
      CreateStreamRequest createStreamRequest =
          CreateStreamRequest.builder().streamName(AUDIT_STREAM_NAME).shardCount(1).build();
      kinesisAsyncClient.createStream(createStreamRequest).get();
    } catch (ResourceInUseException e) {
      log.warn("Error creating kinesis stream?", e);
    }

    kinesisAsyncClient
        .waiter()
        .waitUntilStreamExists(
            DescribeStreamRequest.builder().streamName(AUDIT_STREAM_NAME).build())
        .get();
    return kinesisAsyncClient;
  }

  public static void setSpringProperties(
      DynamicPropertyRegistry registry,
      OpensearchContainer opensearchContainer,
      LocalStackContainer localStackContainer) {
    log.info(
        "Set spring properties : \n spring.elasticsearch.rest.uris : {} \n aws.stream_name : {} \n "
            + "aws.dynamodb.endpoint : {} \n aws.kinesis.endpoint :{} \n aws.cloudwatch.endpoint : {} ",
        opensearchContainer.getHttpHostAddress(),
        AUDIT_STREAM_NAME,
        localStackContainer.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString(),
        localStackContainer.getEndpointOverride(LocalStackContainer.Service.KINESIS).toString(),
        localStackContainer.getEndpointOverride(LocalStackContainer.Service.CLOUDWATCH).toString());

    registry.add("opensearch.endpoint", opensearchContainer::getHttpHostAddress);
    registry.add("aws.stream_name", () -> AUDIT_STREAM_NAME);
    registry.add(
        "aws.dynamodb.endpoint",
        () ->
            localStackContainer
                .getEndpointOverride(LocalStackContainer.Service.DYNAMODB)
                .toString());
    registry.add(
        "aws.kinesis.endpoint",
        () ->
            localStackContainer
                .getEndpointOverride(LocalStackContainer.Service.KINESIS)
                .toString());
    registry.add(
        "aws.cloudwatch.endpoint",
        () ->
            localStackContainer
                .getEndpointOverride(LocalStackContainer.Service.CLOUDWATCH)
                .toString());
  }
}
