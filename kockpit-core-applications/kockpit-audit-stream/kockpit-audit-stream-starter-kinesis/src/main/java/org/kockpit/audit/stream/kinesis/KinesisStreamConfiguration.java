package org.kockpit.audit.stream.kinesis;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kockpit.audit.stream.kinesis.coordination.DynamoDbShardCoordinator;
import org.kockpit.audit.stream.kinesis.coordination.LeaseHeartbeatService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

@AutoConfiguration
@EnableAsync
@Slf4j
public class KinesisStreamConfiguration {

    @Bean("kinesisConsumerClient")
    KinesisAsyncClient amazonKinesis(
            @Value("${kockpit.audit.stream.kinesis.endpoint:}") Optional<String> kinesisEndpointOptional,
            @Value("${aws.region}") String awsRegion,
            @Value("${kockpit.audit.stream.kinesis.timeout.connection:5000}") int connectionTimeoutMs,
            @Value("${kockpit.audit.stream.kinesis.timeout.socket:30000}") int socketTimeoutMs
    ) {
        Region region = Region.of(awsRegion);
        return kinesisEndpointOptional
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .map(kinesisEndpoint -> {
                    log.info("➡️ Kinesis endpoint: {}", kinesisEndpoint);
                    ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                            .apiCallTimeout(Duration.ofMillis(socketTimeoutMs))
                            .apiCallAttemptTimeout(Duration.ofMillis(connectionTimeoutMs))
                            .build();

                    return KinesisAsyncClient.builder()
                            .endpointOverride(URI.create(kinesisEndpoint))
                            .region(region)
                            .overrideConfiguration(overrideConfig)
                            .build();
                }).orElseGet(() -> {
                    log.info("➡️ Initialize Kinesis client using AWS Credentials");
                    return KinesisAsyncClient.builder()
                            .region(region)
                            .credentialsProvider(credentialsProvider())
                            .build();
                });
    }

    @Bean
    DynamoDbAsyncClient dynamoDbClient(
            @Value("${kockpit.audit.stream.dynamodb.endpoint:}") Optional<String> dynamoDbEndpointOptional,
            @Value("${aws.region}") String awsRegion,
            @Value("${kockpit.audit.stream.dynamodb.timeout.connection:5000}") int connectionTimeoutMs,
            @Value("${kockpit.audit.stream.dynamodb.timeout.socket:30000}") int socketTimeoutMs
    ) {
        return dynamoDbEndpointOptional
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .map(dynamoDbEndpoint -> {
                    log.info("➡️ DynamoDb endpoint: {}", dynamoDbEndpoint);
                    ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                            .apiCallTimeout(Duration.ofMillis(socketTimeoutMs))
                            .apiCallAttemptTimeout(Duration.ofMillis(connectionTimeoutMs))
                            .build();

                    return DynamoDbAsyncClient.builder()
                            .endpointOverride(URI.create(dynamoDbEndpoint))
                            .region(Region.of(awsRegion))
                            .overrideConfiguration(overrideConfig)
                            .build();
                }).orElseGet(() -> {
                    log.info("➡️ Initialize DynamoDb client using AWS Credentials");
                    return DynamoDbAsyncClient.builder()
                            .region(Region.of(awsRegion))
                            .credentialsProvider(credentialsProvider())
                            .build();
                });
    }

    @Bean
    CloudWatchAsyncClient cloudWatchClient(
            @Value("${kockpit.audit.stream.cloudwatch.endpoint:}") Optional<String> cloudWatchEndpointOptional,
            @Value("${aws.region}") String awsRegion,
            @Value("${kockpit.audit.stream.cloudwatch.timeout.connection:5000}") int connectionTimeoutMs,
            @Value("${kockpit.audit.stream.cloudwatch.timeout.socket:30000}") int socketTimeoutMs
    ) {
        return cloudWatchEndpointOptional
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .map(cloudWatchEndpoint -> {
                    log.info("➡️ CloudWatch endpoint: {}", cloudWatchEndpoint);
                    ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                            .apiCallTimeout(Duration.ofMillis(socketTimeoutMs))
                            .apiCallAttemptTimeout(Duration.ofMillis(connectionTimeoutMs))
                            .build();

                    return CloudWatchAsyncClient.builder()
                            .endpointOverride(URI.create(cloudWatchEndpoint))
                            .region(Region.of(awsRegion))
                            .overrideConfiguration(overrideConfig)
                            .build();
                }).orElseGet(() -> {
                    log.info("➡️ Initialize CloudWatch client using AWS Credentials");
                    return CloudWatchAsyncClient.builder()
                            .region(Region.of(awsRegion))
                            .credentialsProvider(credentialsProvider())
                            .build();
                });
    }

    AwsCredentialsProvider credentialsProvider() {
        // EC2 instance role -> InstanceProfileCredentialsProvider.builder().build()
        // ECS task role -> ContainerProvider.builder().build()
        return DefaultCredentialsProvider.builder().build();  // Auto-detects IAM role
    }

    @Bean
    DynamoDbShardCoordinator shardCoordinator(
            DynamoDbAsyncClient dynamoDbClient,
            @Value("${kockpit.audit.stream.kinesis.stream_name}") String streamName,
            @Value("${kockpit.audit.stream.kinesis.application_name}") String applicationName,
            @Value("${kockpit.audit.stream.kinesis.worker_id:#{T(java.util.UUID).randomUUID().toString()}}") String workerId,
            @Value("${kockpit.audit.stream.kinesis.lease_duration:PT30S}") Duration leaseDuration
    ) {
        String tableName = streamName + "-" + applicationName + "-coordination";
        return new DynamoDbShardCoordinator(dynamoDbClient, tableName, applicationName, workerId, leaseDuration);
    }

    @Bean
    LeaseHeartbeatService leaseHeartbeatService(DynamoDbShardCoordinator shardCoordinator) {
        return new LeaseHeartbeatService(shardCoordinator);
    }

    @Bean
    KinesisStreamProcessor kinesisStreamProcessor(
            @Qualifier("kinesisConsumerClient") KinesisAsyncClient kinesisAsyncClient,
            @Value("${kockpit.audit.stream.kinesis.stream_name}") String streamName,
            @Value("${kockpit.audit.stream.kinesis.application_name}") String applicationName,
            @Value("${kockpit.audit.stream.kinesis.record_limit:100}") int recordLimit,
            @Value("${kockpit.audit.stream.kinesis.shard_acquisition_interval_ms:30000}") long shardAcquisitionIntervalMs,
            ApplicationEventPublisher applicationEventPublisher,
            DynamoDbShardCoordinator shardCoordinator,
            LeaseHeartbeatService heartbeatService
    ) {
        return new KinesisStreamProcessor(
                kinesisAsyncClient,
                streamName,
                applicationName,
                new KclRecordProcessor(applicationEventPublisher),
                applicationEventPublisher,
                recordLimit,
                shardCoordinator,
                heartbeatService,
                shardAcquisitionIntervalMs
        );
    }

}
