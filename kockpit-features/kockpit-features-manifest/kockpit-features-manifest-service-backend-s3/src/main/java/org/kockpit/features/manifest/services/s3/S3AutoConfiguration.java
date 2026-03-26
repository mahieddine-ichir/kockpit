package org.kockpit.features.manifest.services.s3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@AutoConfiguration
@Slf4j
public class S3AutoConfiguration {

    AwsCredentialsProvider awsCredentialsProvider() {
        return DefaultCredentialsProvider.builder().build();
    }

    // fixme keep?
    @Bean
    @ConditionalOnMissingBean(S3Client.class)
    S3Client s3Client(
            @Value("${kockpit.aws.region}") String region
    ) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(awsCredentialsProvider())
                .build();
    }

    @Bean
    S3Repository s3Repository(
            S3Client s3Client,
            @Value("${kockpit.manifests.aws.s3.bucket}") String bucketName,
            @Value("${kockpit.manifests.aws.region}") String region
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
