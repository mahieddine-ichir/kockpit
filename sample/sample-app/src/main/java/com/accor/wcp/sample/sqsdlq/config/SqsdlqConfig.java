package com.accor.wcp.sample.sqsdlq.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@Profile("aws")
public class SqsdlqConfig {

    @Value("${sqsdlq.awsRegion:}")
    private String sqsAWSRegion;

    @Bean("sqsdlqClient")
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .httpClientBuilder(ApacheHttpClient.builder())
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(sqsAWSRegion))
                .build();
    }
}
