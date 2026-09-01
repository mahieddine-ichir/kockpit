package org.kockpit.audit.stream.kinesis.efo;

import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.common.KinesisClientUtil;
import software.amazon.kinesis.coordinator.Scheduler;
import software.amazon.kinesis.retrieval.fanout.FanOutConfig;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

@AutoConfiguration
@Slf4j
public class EfoKinesisStreamConfiguration {

    @Bean("kinesisEfoConsumerClient")
    KinesisAsyncClient amazonKinesisEfo(
            @Value("${kockpit.audit.stream.kinesis.endpoint:}") Optional<String> kinesisEndpointOptional,
            @Value("${aws.region}") String awsRegion,
            @Value("${kockpit.audit.stream.kinesis.timeout.connection:5000}") int connectionTimeoutMs,
            @Value("${kockpit.audit.stream.kinesis.timeout.socket:30000}") int socketTimeoutMs
    ) {
        Region region = Region.of(awsRegion);
        // KinesisClientUtil applies the HTTP/2 maxConcurrency/window tuning KCL's Enhanced
        // Fan-Out retrieval needs; without it, EFO subscriptions can see significantly higher
        // latency once there are more than a handful of concurrent shard subscriptions.
        return kinesisEndpointOptional
                .map(String::trim)
                .filter(StringUtils::hasLength)
                .map(kinesisEndpoint -> {
                    log.info("➡️ Kinesis (EFO) endpoint: {}", kinesisEndpoint);
                    ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                            .apiCallTimeout(Duration.ofMillis(socketTimeoutMs))
                            .apiCallAttemptTimeout(Duration.ofMillis(connectionTimeoutMs))
                            .build();

                    return KinesisClientUtil.createKinesisAsyncClient(KinesisAsyncClient.builder()
                            .endpointOverride(URI.create(kinesisEndpoint))
                            .region(region)
                            .overrideConfiguration(overrideConfig));
                }).orElseGet(() -> {
                    log.info("➡️ Initialize Kinesis (EFO) client using AWS Credentials");
                    return KinesisClientUtil.createKinesisAsyncClient(KinesisAsyncClient.builder()
                            .region(region)
                            .credentialsProvider(credentialsProvider()));
                });
    }

    @Bean("efoDynamoDbClient")
    DynamoDbAsyncClient efoDynamoDbClient(
            @Value("${kockpit.audit.stream.dynamodb.endpoint:}") Optional<String> dynamoDbEndpointOptional,
            @Value("${aws.region}") String awsRegion,
            @Value("${kockpit.audit.stream.dynamodb.timeout.connection:5000}") int connectionTimeoutMs,
            @Value("${kockpit.audit.stream.dynamodb.timeout.socket:30000}") int socketTimeoutMs
    ) {
        return dynamoDbEndpointOptional
                .map(String::trim)
                .filter(StringUtils::hasLength)
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

    @Bean("efoCloudWatchClient")
    CloudWatchAsyncClient efoCloudWatchClient(
            @Value("${kockpit.audit.stream.cloudwatch.endpoint:}") Optional<String> cloudWatchEndpointOptional,
            @Value("${aws.region}") String awsRegion,
            @Value("${kockpit.audit.stream.cloudwatch.timeout.connection:5000}") int connectionTimeoutMs,
            @Value("${kockpit.audit.stream.cloudwatch.timeout.socket:30000}") int socketTimeoutMs
    ) {
        return cloudWatchEndpointOptional
                .map(String::trim)
                .filter(StringUtils::hasLength)
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
    Scheduler kinesisEfoScheduler(
            @Qualifier("kinesisEfoConsumerClient") KinesisAsyncClient kinesisAsyncClient,
            @Qualifier("efoDynamoDbClient") DynamoDbAsyncClient dynamoDbAsyncClient,
            @Qualifier("efoCloudWatchClient") CloudWatchAsyncClient cloudWatchAsyncClient,
            @Value("${kockpit.audit.stream.kinesis.stream_name}") String streamName,
            @Value("${kockpit.audit.stream.kinesis.application_name}") String applicationName,
            @Value("${kockpit.audit.stream.kinesis.worker_id:#{T(java.util.UUID).randomUUID().toString()}}") String workerId,
            @Value("${kockpit.audit.stream.kinesis.efo.consumer_name}") String consumerName,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        ConfigsBuilder configsBuilder = new ConfigsBuilder(
                streamName,
                applicationName,
                kinesisAsyncClient,
                dynamoDbAsyncClient,
                cloudWatchAsyncClient,
                workerId,
                new AuditRecordProcessorFactory(applicationEventPublisher)
        );

        // consumerName is kept distinct from applicationName (which names the lease table and
        // CloudWatch namespace) so the registered stream consumer has its own recognizable name.
        // KCL registers/reuses this consumer and waits for it to become ACTIVE on startup.
        FanOutConfig fanOutConfig = new FanOutConfig(kinesisAsyncClient)
                .consumerName(consumerName);

        return new Scheduler(
                configsBuilder.checkpointConfig(),
                configsBuilder.coordinatorConfig(),
                configsBuilder.leaseManagementConfig(),
                configsBuilder.lifecycleConfig(),
                configsBuilder.metricsConfig(),
                configsBuilder.processorConfig(),
                configsBuilder.retrievalConfig().retrievalSpecificConfig(fanOutConfig)
        );
    }

    @Bean
    KinesisEfoSchedulerLifecycle kinesisEfoSchedulerLifecycle(Scheduler kinesisEfoScheduler) {
        return new KinesisEfoSchedulerLifecycle(kinesisEfoScheduler);
    }
}
