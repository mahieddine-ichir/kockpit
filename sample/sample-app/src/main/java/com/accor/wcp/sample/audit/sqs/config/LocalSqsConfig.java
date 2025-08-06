package com.accor.wcp.sample.audit.sqs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
@Profile({"dev","local"})
public class LocalSqsConfig {

    @Value("${sqs.awsRegion:}")
    private String sqsAWSRegion;

    @Value("${sqs.sqsEndpointUrl:}")
    private String sqsEndpointUrl;

    @Bean("sqsClient")
    public SqsClient amazonSQS() {
        return SqsClient.builder()
                .httpClientBuilder(ApacheHttpClient.builder())
                .endpointOverride(URI.create(sqsEndpointUrl))
                .region(Region.of(sqsAWSRegion))
                .build();
    }
}
