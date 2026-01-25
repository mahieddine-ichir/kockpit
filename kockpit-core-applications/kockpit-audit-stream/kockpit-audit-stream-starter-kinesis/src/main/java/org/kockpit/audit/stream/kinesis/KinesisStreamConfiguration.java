package org.kockpit.audit.stream.kinesis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;

import java.net.URI;
import java.time.Duration;

@Configuration
@EnableAsync
public class KinesisStreamConfiguration {

    @Value("${aws.kinesis.endpoint:http://localhost:4566}")
    private String kinesisEndpoint;

    @Value("${aws.region:eu-west-1}")
    private String awsRegion;

    @Value("${kockpit.audit.stream.kinesis.timeout.connection:5000}")
    private int connectionTimeoutMs;

    @Value("${kockpit.audit.stream.kinesis.timeout.socket:30000}")
    private int socketTimeoutMs;

    @Bean
    KinesisClient amazonKinesis(AwsCredentialsProvider credentialsProvider) {
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
    public CommandLineRunner initKinesis(KinesisStreamListener listener) {
        return args -> listener.startAsync();
    }
}
