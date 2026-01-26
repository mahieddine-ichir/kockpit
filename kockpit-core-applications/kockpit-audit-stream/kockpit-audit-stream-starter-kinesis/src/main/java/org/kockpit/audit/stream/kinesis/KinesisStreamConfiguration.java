package org.kockpit.audit.stream.kinesis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
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

@AutoConfiguration
@EnableAsync
public class KinesisStreamConfiguration {

    @Bean("kinesisConsumerClient")
    KinesisAsyncClient amazonKinesis(
            @Value("${kockpit.audit.stream.kinesis.endpoint:}") String kinesisEndpoint,
            @Value("${aws.region}") String awsRegion,
            @Value("${kockpit.audit.stream.kinesis.timeout.connection:5000}") int connectionTimeoutMs,
            @Value("${kockpit.audit.stream.kinesis.timeout.socket:30000}") int socketTimeoutMs
    ) {
        Region region = Region.of(awsRegion);
        if (kinesisEndpoint == null || kinesisEndpoint.isEmpty()) {
            return KinesisAsyncClient.builder()
                    .region(region)
                    .credentialsProvider(credentialsProvider())
                    .build();
        } else {
            ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                    .apiCallTimeout(Duration.ofMillis(socketTimeoutMs))
                    .apiCallAttemptTimeout(Duration.ofMillis(connectionTimeoutMs))
                    .build();

            return KinesisAsyncClient.builder()
                    .endpointOverride(URI.create(kinesisEndpoint))
                    .region(region)
                    .overrideConfiguration(overrideConfig)
                    .build();
        }
    }

    @Bean
    DynamoDbAsyncClient dynamoDbClient(
            @Value("${kockpit.audit.stream.dynamodb.endpoint:}") String dynamoDbEndpoint,
            @Value("${aws.region}") String awsRegion,
            @Value("${kockpit.audit.stream.dynamodb.timeout.connection:5000}") int connectionTimeoutMs,
            @Value("${kockpit.audit.stream.dynamodb.timeout.socket:30000}") int socketTimeoutMs
    ) {
        if (dynamoDbEndpoint == null || dynamoDbEndpoint.isEmpty()) {
            return DynamoDbAsyncClient.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(credentialsProvider())
                    .build();
        } else {
            ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                    .apiCallTimeout(Duration.ofMillis(socketTimeoutMs))
                    .apiCallAttemptTimeout(Duration.ofMillis(connectionTimeoutMs))
                    .build();

            return DynamoDbAsyncClient.builder()
                    .endpointOverride(URI.create(dynamoDbEndpoint))
                    .region(Region.of(awsRegion))
                    .overrideConfiguration(overrideConfig)
                    .build();
        }
    }

    @Bean
    CloudWatchAsyncClient cloudWatchClient(
            @Value("${kockpit.audit.stream.cloudwatch.endpoint:}") String cloudWatchEndpoint,
            @Value("${aws.region}") String awsRegion,
            @Value("${kockpit.audit.stream.cloudwatch.timeout.connection:5000}") int connectionTimeoutMs,
            @Value("${kockpit.audit.stream.cloudwatch.timeout.socket:30000}") int socketTimeoutMs
    ) {
        if (cloudWatchEndpoint == null || cloudWatchEndpoint.isEmpty()) {
            return CloudWatchAsyncClient.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(credentialsProvider())
                    .build();
        } else {
            ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                    .apiCallTimeout(Duration.ofMillis(socketTimeoutMs))
                    .apiCallAttemptTimeout(Duration.ofMillis(connectionTimeoutMs))
                    .build();

            return CloudWatchAsyncClient.builder()
                    .endpointOverride(URI.create(cloudWatchEndpoint))
                    .region(Region.of(awsRegion))
                    .overrideConfiguration(overrideConfig)
                    .build();
        }
    }

    AwsCredentialsProvider credentialsProvider() {
        // EC2 instance role -> InstanceProfileCredentialsProvider.builder().build()
        // ECS task role -> ContainerProvider.builder().build()
        return DefaultCredentialsProvider.builder().build();  // Auto-detects IAM role
    }

    @Bean
    KclRecordProcessorFactory kclRecordProcessorFactory(ApplicationEventPublisher applicationEventPublisher) {
        return new KclRecordProcessorFactory(applicationEventPublisher);
    }

    @Bean
    KinesisStreamListener kinesisStreamListener(
            @Qualifier("kinesisConsumerClient") KinesisAsyncClient kinesisClient,
            DynamoDbAsyncClient dynamoDbClient,
            CloudWatchAsyncClient cloudWatchClient,
            KclRecordProcessorFactory recordProcessorFactory,
            @Value("${kockpit.audit.stream.kinesis.stream_name}") String streamName,
            @Value("${kockpit.audit.stream.kinesis.application_name}") String applicationName,
            @Value("${kockpit.audit.stream.kinesis.workerIdentifier:#{T(java.util.UUID).randomUUID().toString()}}") String workerIdentifier
    ) {
        return new KinesisStreamListener(
                kinesisClient,
                dynamoDbClient,
                cloudWatchClient,
                recordProcessorFactory,
                streamName,
                applicationName,
                workerIdentifier
        );
    }

    @Bean
    public CommandLineRunner initKinesis(KinesisStreamListener listener) {
        return args -> listener.startAsync();
    }
}
