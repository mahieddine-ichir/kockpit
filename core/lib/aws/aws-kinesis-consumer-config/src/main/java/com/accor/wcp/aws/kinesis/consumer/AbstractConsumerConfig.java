package com.accor.wcp.aws.kinesis.consumer;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.net.URI;
import java.util.UUID;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.common.KinesisClientUtil;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;

@Slf4j
public abstract class AbstractConsumerConfig {

  private final String streamName;
  private final String applicationId;
  private final ShardRecordProcessorFactory recordProcessorFactory;

  @Value(value = "${aws.region:eu-west-1}")
  private String awsRegion;

  @Value("${aws.kinesis.endpoint:}")
  private String kinesisEndpoint;

  @Value("${aws.dynamodb.endpoint:}")
  private String dynamoDbEndpoint;

  @Value("${aws.cloudwatch.endpoint:}")
  private String cloudWatchEndpoint;

  private ConfigsBuilder configsBuilder;
  private KinesisAsyncClient kinesisClient;

  protected AbstractConsumerConfig(
      String streamName, String applicationId, ShardRecordProcessorFactory recordProcessorFactory) {
    this.streamName = streamName;
    this.applicationId = applicationId;
    this.recordProcessorFactory = recordProcessorFactory;
  }

  @PostConstruct
  void init() {
    Region region = Region.of(awsRegion);

    CloudWatchAsyncClient cloudWatchClient;
    DynamoDbAsyncClient dynamoClient;
    if (isNotEmpty(getKinesisEndpoint())) {
      // Special case for local dev
      kinesisClient =
          KinesisClientUtil.createKinesisAsyncClient(
              KinesisAsyncClient.builder()
                  .endpointOverride(URI.create(getKinesisEndpoint()))
                  .region(region));

      dynamoClient =
          DynamoDbAsyncClient.builder()
              .endpointOverride(URI.create(dynamoDbEndpoint))
              .region(region)
              .build();

      cloudWatchClient =
          CloudWatchAsyncClient.builder()
              .endpointOverride(URI.create(cloudWatchEndpoint))
              .region(region)
              .build();
    } else {
      AwsCredentialsProvider kinesisCredentialProvider = getKinesisCredentialProvider();
      // Normal AWS env
      kinesisClient =
          KinesisClientUtil.createKinesisAsyncClient(
              KinesisAsyncClient.builder()
                  .credentialsProvider(kinesisCredentialProvider)
                  .region(region));

      AwsCredentialsProvider dynamoDbCredentialsProvider = getDynamoDbCredentialsProvider();
      dynamoClient =
          DynamoDbAsyncClient.builder()
              .credentialsProvider(dynamoDbCredentialsProvider)
              .region(region)
              .build();

      AwsCredentialsProvider cloudWatchCredentialsProvider = getCloudWatchCredentialsProvider();
      cloudWatchClient =
          CloudWatchAsyncClient.builder()
              .credentialsProvider(cloudWatchCredentialsProvider)
              .region(region)
              .build();
    }

    configsBuilder =
        new ConfigsBuilder(
            streamName,
            applicationId,
            kinesisClient,
            dynamoClient,
            cloudWatchClient,
            UUID.randomUUID().toString(),
            recordProcessorFactory);
    postConfigsBuilder(configsBuilder);
  }

  protected AwsCredentialsProvider getKinesisCredentialProvider() {
    return DefaultCredentialsProvider.create();
  }

  protected AwsCredentialsProvider getDynamoDbCredentialsProvider() {
    return DefaultCredentialsProvider.create();
  }

  protected AwsCredentialsProvider getCloudWatchCredentialsProvider() {
    return DefaultCredentialsProvider.create();
  }

  protected String getKinesisEndpoint() {
    return kinesisEndpoint;
  }

  protected void postConfigsBuilder(ConfigsBuilder configsBuilder) {
    configsBuilder.leaseManagementConfig().initialLeaseTableReadCapacity(1);
    configsBuilder.leaseManagementConfig().initialLeaseTableWriteCapacity(1);
  }

  public String getStreamName() {
    return streamName;
  }

  public KinesisAsyncClient getKinesisClient() {
    return kinesisClient;
  }

  public ConfigsBuilder getConfigBuilder() {
    return configsBuilder;
  }

  public String getApplicationId() {
    return applicationId;
  }
}
