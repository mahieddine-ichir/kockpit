package com.accor.wcp.sample.audit.kinesis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;

@Configuration
@Profile("aws")
public class KinesisConfig {

    @Value("${kinesis.awsRegion:}")
    private String awsRegion;

    @Bean("kinesisClient")
    public KinesisClient kinesisClient() {
        return KinesisClient.builder()
                .httpClientBuilder(ApacheHttpClient.builder())
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(awsRegion))
                .build();
    }
}
