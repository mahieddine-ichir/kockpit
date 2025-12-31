package org.kockpit.features.manifest.services.s3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@AutoConfiguration
@Slf4j
public class S3AutoConfiguration {

    @Bean
    AwsCredentialsProvider awsCredentialsProvider() {
        return DefaultCredentialsProvider.builder().build();
    }

    @Bean
    S3Client s3Client(
            @Value("${kockpit.sdk.aws.region}") String region,
            AwsCredentialsProvider credentialsProvider
    ) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    S3Repository s3Repository(
            S3Client s3Client,
            @Value("${kockpit.sdk.aws.s3.bucket}") String bucketName,
            @Value("${kockpit.sdk.aws.region}") String region
    ) {
        log.info(
"""
    \n
    - S3 config repository, bucket: {}
    - Region: {}
""", bucketName, region);
        return new S3Repository(s3Client, bucketName);
    }
}
