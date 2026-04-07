package org.kockpit.communication.legacy;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@ConditionalOnProperty(
        value = "kockpit.legacy.enabled",
        havingValue = "true"
)
@AutoConfiguration
public class LegacyAutoConfiguration {

    @Bean
    DynaConfigLegacyPublisher dynaConfigLegacyPublisher(
            S3Client s3Client,
            @Value("${kockpit.legacy.aws.s3.bucket}") String bucketName
    ) {
        return new DynaConfigLegacyPublisher(
                s3Client,
                bucketName,
                new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
        );
    }

    AwsCredentialsProvider awsCredentialsProvider() {
        return DefaultCredentialsProvider.builder().build();
    }

    @Bean
    @ConditionalOnMissingBean(S3Client.class)
    S3Client s3Client(
            @Value("${aws.region}") String region
    ) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(awsCredentialsProvider())
                .build();
    }
}
