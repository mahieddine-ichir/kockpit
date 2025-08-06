package com.accor.wcp.console.services.core.integration.utils;

import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
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
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

@Slf4j
@SpringBootTest
@ActiveProfiles({"local", "test"})
public class BaseIt {

  @Autowired ObjectMapper objectMapper;

  private static final String LOCALSTACK_IMAGE = "localstack/localstack:3.8.0";
  private static final int EDGE_PORT = 4566;

  static LocalStackContainer initLocalStackContainer() {
    LocalStackContainer localStackContainer =
        new LocalStackContainer(DockerImageName.parse(LOCALSTACK_IMAGE))
            .withServices(
                LocalStackContainer.Service.DYNAMODB,
                LocalStackContainer.Service.CLOUDWATCH,
                LocalStackContainer.Service.SQS,
                LocalStackContainer.Service.S3,
                LocalStackContainer.Service.SNS,
                LocalStackContainer.Service.KINESIS)
            .withExposedPorts(EDGE_PORT)
            .withEnv("EDGE_PORT", String.valueOf(EDGE_PORT));
    localStackContainer.start();
    log.info(
        "LocalStackContainer started, name : {}", localStackContainer.getContainerInfo().getName());
    return localStackContainer;
  }

  static S3Client initS3Client(URI endpoint) {
    Region region = Region.EU_WEST_1;
    S3Client s3Client =
        S3Client.builder()
            .region(region)
            .endpointOverride(endpoint)
            .httpClientBuilder(ApacheHttpClient.builder())
            .build();
    CreateBucketRequest createBucketRequest =
        CreateBucketRequest.builder().bucket("wcp-sdk-bucket-wcp2app-local").build();
    s3Client.createBucket(createBucketRequest);
    return s3Client;
  }

  static SqsClient initSqsClient(URI endpoint) {
    SqsClient sqsClient =
        SqsClient.builder()
            .httpClientBuilder(ApacheHttpClient.builder())
            .endpointOverride(endpoint)
            .region(Region.EU_WEST_1)
            .build();

    Map<QueueAttributeName, String> attributes = new HashMap<>();
    attributes.put(QueueAttributeName.FIFO_QUEUE, Boolean.TRUE.toString());

    CreateQueueRequest createQueueRequest =
        CreateQueueRequest.builder().attributes(attributes).queueName("testdlq.fifo").build();
    sqsClient.createQueue(createQueueRequest);
    return sqsClient;
  }

  static DynamoDbTable<SqsDocumentV2> initDynamoDB(URI endpoint) {
    DynamoDbClient dynamoDbClient =
        DynamoDbClient.builder()
            .httpClientBuilder(ApacheHttpClient.builder())
            .endpointOverride(endpoint)
            .region(Region.EU_WEST_1)
            .build();

    DynamoDbEnhancedClient dynamoDbEnhancedClient =
        DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();

    DynamoDbTable<SqsDocumentV2> sqsDocumentV2 =
        dynamoDbEnhancedClient.table("SqsDocumentV2", TableSchema.fromBean(SqsDocumentV2.class));

    CreateTableEnhancedRequest request = CreateTableEnhancedRequest.builder().build();
    sqsDocumentV2.createTable(request);
    return sqsDocumentV2;
  }

  public static void setSpringProperties(
      DynamicPropertyRegistry registry, LocalStackContainer localStackContainer) {
    log.info(
        "Set spring properties : \n aws.dynamodb.endpoint : {} \n "
            + "aws.cloudwatch.endpoint : {} \n aws.sqs.endpoint : {} \n aws.s3.endpoint : {}",
        localStackContainer.getEndpointOverride(LocalStackContainer.Service.DYNAMODB).toString(),
        localStackContainer.getEndpointOverride(LocalStackContainer.Service.CLOUDWATCH).toString(),
        localStackContainer.getEndpointOverride(LocalStackContainer.Service.SQS).toString(),
        localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3).toString());

    registry.add(
        "aws.dynamodb.endpoint",
        () ->
            localStackContainer
                .getEndpointOverride(LocalStackContainer.Service.DYNAMODB)
                .toString());
    registry.add(
        "aws.cloudwatch.endpoint",
        () ->
            localStackContainer
                .getEndpointOverride(LocalStackContainer.Service.CLOUDWATCH)
                .toString());
    registry.add(
        "aws.sqs.endpoint",
        () -> localStackContainer.getEndpointOverride(LocalStackContainer.Service.SQS).toString());
    registry.add(
        "aws.s3.endpoint",
        () -> localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3).toString());
    registry.add(
            "aws.kinesis.endpoint",
            () -> localStackContainer.getEndpointOverride(LocalStackContainer.Service.KINESIS).toString());

  }
}
