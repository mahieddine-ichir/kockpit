package org.kockpit.audit.stream.kinesis.efo;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.Protocol;
import software.amazon.awssdk.http.nio.netty.Http2Configuration;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.coordinator.Scheduler;
import software.amazon.kinesis.lifecycle.LifecycleConfig;
import software.amazon.kinesis.retrieval.fanout.FanOutConfig;

import java.net.URI;
import java.time.Duration;
import java.util.List;
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

        // Mirrors KinesisClientUtil.adjustKinesisClientBuilder's HTTP/2 tuning (maxConcurrency,
        // initial window size, health-check ping) - KCL's Enhanced Fan-Out retrieval needs it,
        // without it EFO subscriptions see significantly higher latency once there are more than
        // a handful of concurrent shard subscriptions. Built here instead of delegating to
        // KinesisClientUtil because that helper doesn't expose a way to also set
        // readTimeout/writeTimeout, which is what actually governs the
        // "ShardConsumerSubscriber: onError() ... ReadTimeout" warning KCL logs when a shard's
        // EFO subscription (a long-lived HTTP/2 stream) goes idle too long - a Netty socket-level
        // read timeout, not the SDK's apiCallAttemptTimeout (a different, unrelated mechanism for
        // this kind of streaming call). connectionTimeoutMs/socketTimeoutMs previously fed
        // ClientOverrideConfiguration.apiCallAttemptTimeout/apiCallTimeout - the wrong knob for
        // this - and only in the endpoint-override branch below, never for the real-AWS
        // (production) path.
        NettyNioAsyncHttpClient.Builder httpClientBuilder = NettyNioAsyncHttpClient.builder()
                .maxConcurrency(Integer.MAX_VALUE)
                .http2Configuration(Http2Configuration.builder()
                        .initialWindowSize(512 * 1024)
                        .healthCheckPingPeriod(Duration.ofMillis(60_000))
                        .build())
                .protocol(Protocol.HTTP2)
                .connectionTimeout(Duration.ofMillis(connectionTimeoutMs))
                .readTimeout(Duration.ofMillis(socketTimeoutMs))
                .writeTimeout(Duration.ofMillis(socketTimeoutMs));

        var builder = KinesisAsyncClient.builder()
                .region(region)
                .httpClientBuilder(httpClientBuilder);

        return kinesisEndpointOptional
                .map(String::trim)
                .filter(StringUtils::hasLength)
                .map(kinesisEndpoint -> {
                    log.info("➡️ Kinesis (EFO) endpoint: {}", kinesisEndpoint);
                    return builder.endpointOverride(URI.create(kinesisEndpoint)).build();
                }).orElseGet(() -> {
                    log.info("➡️ Initialize Kinesis (EFO) client using AWS Credentials");
                    return builder.credentialsProvider(credentialsProvider()).build();
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
            @Value("${kockpit.audit.stream.kinesis.efo.checkpoint_interval:1}") int checkpointIntervalBatches,
            @Value("${kockpit.audit.stream.kinesis.efo.read_timeouts_to_ignore:3}") int readTimeoutsToIgnoreBeforeWarning,
            List<AuditConsumer> auditConsumers
    ) {
        ConfigsBuilder configsBuilder = new ConfigsBuilder(
                streamName,
                applicationName,
                kinesisAsyncClient,
                dynamoDbAsyncClient,
                cloudWatchAsyncClient,
                workerId,
                new AuditRecordProcessorFactory(auditConsumers, checkpointIntervalBatches)
        );

        // consumerName is kept distinct from applicationName (which names the lease table and
        // CloudWatch namespace) so the registered stream consumer has its own recognizable name.
        // KCL registers/reuses this consumer and waits for it to become ACTIVE on startup.
        FanOutConfig fanOutConfig = new FanOutConfig(kinesisAsyncClient)
                .consumerName(consumerName);

        // KCL's default (0) logs a WARN on the very first Netty ReadTimeout on a shard's EFO
        // subscription, even though onError() already cancels and transparently recreates the
        // subscription on its own - an idle/renewed HTTP2 stream is expected, recoverable
        // behavior, not an operational problem. Tolerating a few before warning cuts that noise
        // without hiding a shard that's timing out persistently (each occurrence still resets the
        // count to 0 on the next successful read - see ShardConsumerSubscriber.onNext).
        LifecycleConfig lifecycleConfig = configsBuilder.lifecycleConfig()
                .readTimeoutsToIgnoreBeforeWarning(readTimeoutsToIgnoreBeforeWarning);

        return new Scheduler(
                configsBuilder.checkpointConfig(),
                configsBuilder.coordinatorConfig(),
                configsBuilder.leaseManagementConfig(),
                lifecycleConfig,
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
