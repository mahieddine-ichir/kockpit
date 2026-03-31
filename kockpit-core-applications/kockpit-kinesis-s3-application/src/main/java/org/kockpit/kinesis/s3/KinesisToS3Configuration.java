package org.kockpit.kinesis.s3;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.util.Optional;

@Configuration
@Slf4j
public class KinesisToS3Configuration {

    @Bean
    S3Client s3Client(
            @Value("${kockpit.kinesis-to-s3.s3.endpoint:}") Optional<String> endpointOpt,
            @Value("${aws.region}") String awsRegion
    ) {
        return endpointOpt
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(endpoint -> {
                    log.info("➡️ S3 endpoint override: {}", endpoint);
                    return S3Client.builder()
                            .endpointOverride(URI.create(endpoint))
                            .region(Region.of(awsRegion))
                            .forcePathStyle(true)
                            .build();
                }).orElseGet(() -> {
                    log.info("➡️ Initialize S3 client using AWS Credentials");
                    return S3Client.builder()
                            .region(Region.of(awsRegion))
                            .credentialsProvider(DefaultCredentialsProvider.builder().build())
                            .build();
                });
    }

    @Bean
    KinesisToS3Consumer kinesisToS3Consumer(
            S3Client s3Client,
            @Value("${kockpit.kinesis-to-s3.s3.bucket}") String bucketName,
            @Value("${kockpit.kinesis-to-s3.s3.key-prefix:records}") String keyPrefix
    ) {
        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .registerModule(new JavaTimeModule());
        return new KinesisToS3Consumer(s3Client, bucketName, keyPrefix, objectMapper);
    }
}
