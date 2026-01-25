package org.kockpit.audit.stream.kinesis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import software.amazon.awssdk.services.kinesis.KinesisClient;

import java.net.URI;
import java.time.Duration;

@AutoConfiguration
@EnableAsync
public class KinesisStreamConfiguration {

    @Bean
    KinesisClient amazonKinesis(
            @Value("${aws.kinesis.endpoint:http://localhost:4566}") String kinesisEndpoint,
            @Value("${aws.region:eu-west-1}") String awsRegion,
            @Value("${kockpit.audit.stream.kinesis.timeout.connection:5000}") int connectionTimeoutMs,
            @Value("${kockpit.audit.stream.kinesis.timeout.socket:30000}") int socketTimeoutMs,
            AwsCredentialsProvider credentialsProvider) {
        ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                .apiCallTimeout(Duration.ofMillis(socketTimeoutMs))
                .apiCallAttemptTimeout(Duration.ofMillis(connectionTimeoutMs))
                .build();

        return KinesisClient.builder()
                .endpointOverride(URI.create(kinesisEndpoint))
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .overrideConfiguration(overrideConfig)
                .build();
    }

    @Bean
    AwsCredentialsProvider credentialsProvider() {
        // EC2 instance role -> InstanceProfileCredentialsProvider.builder().build()
        // ECS task role -> ContainerProvider.builder().build()
        return DefaultCredentialsProvider.builder().build();  // Auto-detects IAM role
    }

    @Bean
    KinesisStreamListener kinesisStreamListener(
            KinesisClient kinesisClient,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        return new KinesisStreamListener(
                kinesisClient,
                applicationEventPublisher,
                new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .registerModule(new JavaTimeModule())
        );
    }

    @Bean
    public CommandLineRunner initKinesis(KinesisStreamListener listener) {
        return args -> listener.startAsync();
    }
}
