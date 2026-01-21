package org.kockpit.communication.s3;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kockpit.communication.Consumer;
import org.kockpit.communication.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@AutoConfiguration
@ConditionalOnProperty(
        value = "kockpit.communication.s3.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class S3CommunicationAutoConfiguration {

    @Bean
    Publisher s3Publisher(
            S3Client s3Client,
            @Value("${kockpit.sdk.aws.s3.bucket}") String bucketName
    ) {
        return new S3Publisher(s3Client, bucketName, objectMapper());
    }

    @Bean
    Consumer s3Consumer(
            S3Client s3Client,
            @Value("${kockpit.sdk.aws.s3.bucket}") String bucketName
    ) {
        return new S3Consumer(s3Client, bucketName, objectMapper());
    }

    ObjectMapper objectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    }

    AwsCredentialsProvider awsCredentialsProvider() {
        return DefaultCredentialsProvider.builder().build();
    }

    @Bean
    @Primary
    S3Client s3Client(
            @Value("${kockpit.sdk.aws.region}") String region
    ) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(awsCredentialsProvider())
                .build();
    }
}
