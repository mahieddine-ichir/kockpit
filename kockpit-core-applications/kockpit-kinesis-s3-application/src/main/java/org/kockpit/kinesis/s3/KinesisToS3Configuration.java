package org.kockpit.kinesis.s3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.s3.S3Client;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class KinesisToS3Configuration {

    @Bean
    KinesisClient kinesisClient(
            @Value("${kockpit.kinesis-to-s3.kinesis.endpoint:}") Optional<String> endpointOpt,
            @Value("${aws.region}") String awsRegion
    ) {
        return endpointOpt
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(endpoint -> {
                    log.info("Kinesis endpoint override: {}", endpoint);
                    return KinesisClient.builder()
                            .endpointOverride(URI.create(endpoint))
                            .region(Region.of(awsRegion))
                            .build();
                }).orElseGet(() -> {
                    log.info("Initializing Kinesis client using AWS credentials");
                    return KinesisClient.builder()
                            .region(Region.of(awsRegion))
                            .credentialsProvider(DefaultCredentialsProvider.create())
                            .build();
                });
    }

    @Bean
    S3Client s3Client(
            @Value("${kockpit.kinesis-to-s3.s3.endpoint:}") Optional<String> endpointOpt,
            @Value("${aws.region}") String awsRegion
    ) {
        return endpointOpt
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(endpoint -> {
                    log.info("S3 endpoint override: {}", endpoint);
                    return S3Client.builder()
                            .endpointOverride(URI.create(endpoint))
                            .region(Region.of(awsRegion))
                            .forcePathStyle(true)
                            .build();
                }).orElseGet(() -> {
                    log.info("Initializing S3 client using AWS credentials");
                    return S3Client.builder()
                            .region(Region.of(awsRegion))
                            .credentialsProvider(DefaultCredentialsProvider.create())
                            .build();
                });
    }

    @Bean
    KinesisToS3Writer kinesisToS3Writer(
            S3Client s3Client,
            @Value("${kockpit.kinesis-to-s3.s3.bucket}") String bucketName
    ) {
        ObjectMapper objectMapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                // Jackson 3 trie les proprietes alphabetiquement par defaut ; on conserve l'ordre
                // de declaration (defaut Jackson 2) pour ne pas changer le JSON produit.
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .build();
        return new KinesisToS3Writer(s3Client, bucketName, objectMapper, new KinesisRecordMapper());
    }

    @Bean(destroyMethod = "shutdown")
    ScheduledExecutorService kinesisReaderScheduler(
            KinesisClient kinesisClient,
            KinesisToS3Writer writer,
            @Value("${kockpit.kinesis-to-s3.kinesis.stream-names}") String streamNamesConfig,
            @Value("${kockpit.kinesis-to-s3.kinesis.record-limit:100}") int recordLimit,
            @Value("${kockpit.kinesis-to-s3.kinesis.read-interval-ms:5000}") long intervalMs
    ) {
        List<String> streamNames = Arrays.stream(streamNamesConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(streamNames.size());
        streamNames.forEach(streamName -> {
            KinesisReader reader = new KinesisReader(kinesisClient, streamName, recordLimit, writer);
            scheduler.scheduleWithFixedDelay(reader::poll, 0, intervalMs, TimeUnit.MILLISECONDS);
        });
        log.info("Started Kinesis readers for streams: {}", streamNames);
        return scheduler;
    }
}