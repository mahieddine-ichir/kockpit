package com.accor.wcp.sample.audit.kinesis.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisClient;

@Configuration
@Profile({"dev", "local"})
public class LocalKinesisConfig {

    @Value("${kinesis.awsRegion:}")
    private String awsRegion;

    @Value("${kinesis.sqsEndpointUrl:}")
    private String endpointUrl;

    @Bean("kinesisClient")
    public KinesisClient amazonKinesis() {
        return KinesisClient.builder()
                .httpClientBuilder(ApacheHttpClient.builder())
                .region(Region.of(awsRegion))
                .endpointOverride(URI.create(endpointUrl))
                .build();
    }
}
