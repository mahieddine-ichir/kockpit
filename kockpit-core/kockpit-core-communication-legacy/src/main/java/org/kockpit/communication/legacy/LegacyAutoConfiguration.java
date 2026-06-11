package org.kockpit.communication.legacy;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.legacy.dynaconfig.DynaConfigLegacyConsumer;
import org.kockpit.communication.legacy.dynaconfig.DynaConfigLegacyPublisher;
import org.kockpit.communication.legacy.featureflipping.FeatureFlippingLegacyPublisher;
import org.kockpit.communication.legacy.health.HealthLegacyConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.util.Optional;

@ConditionalOnProperty(
        value = "kockpit.legacy.enabled",
        havingValue = "true"
)
@AutoConfiguration
@Slf4j
public class LegacyAutoConfiguration {

    @ConditionalOnProperty(
            value = "kockpit.legacy.dyna-config.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    @Bean
    DynaConfigLegacyPublisher dynaConfigLegacyPublisher(
            S3Client s3Client,
            @Value("${kockpit.legacy.aws.s3.bucket}") String bucketName
    ) {
        return new DynaConfigLegacyPublisher(
                s3Client,
                bucketName,
                legacyObjectMapper()
        );
    }

    @ConditionalOnProperty(
            value = "kockpit.legacy.feature-flipping.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    @Bean
    FeatureFlippingLegacyPublisher featureFlippingLegacyPublisher(
            S3Client s3Client,
            @Value("${kockpit.legacy.aws.s3.bucket}") String bucketName
    ) {
        return new FeatureFlippingLegacyPublisher(s3Client, bucketName, legacyObjectMapper());
    }

    //@Bean
    DynaConfigLegacyConsumer dynaConfigLegacyConsumer(
            S3Client s3Client,
            @Value("${kockpit.legacy.aws.s3.bucket}") String bucketName
    ) {
        return new DynaConfigLegacyConsumer(s3Client, bucketName, legacyObjectMapper());
    }

    @Bean
    @ConditionalOnProperty(
            value = "kockpit.legacy.heartbit.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    HealthLegacyConsumer healthLegacyConsumer(
            S3Client s3Client,
            @Value("${kockpit.legacy.aws.s3.bucket}") String bucketName
    ) {
        return new HealthLegacyConsumer(s3Client, bucketName, legacyObjectMapper());
    }

    private ObjectMapper legacyObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    }

    AwsCredentialsProvider awsCredentialsProvider() {
        return DefaultCredentialsProvider.builder().build();
    }

    @Bean
    @ConditionalOnMissingBean(S3Client.class)
    S3Client s3Client(
            @Value("${kockpit.legacy.aws.region}") String region,
            @Value("${kockpit.legacy.aws.s3.endpoint:}") Optional<String> optionalEndpoint
    ) {
        return optionalEndpoint
                .map(String::trim)
                .filter(StringUtils::hasLength)
                .map(endpoint -> {
                    log.info("➡️ s3 legacy endpoint: {}", endpoint);
                    return S3Client.builder()
                            .region(Region.of(region))
                            .endpointOverride(URI.create(endpoint))
                            .crossRegionAccessEnabled(true)
                            .serviceConfiguration(S3Configuration.builder()
                                    .chunkedEncodingEnabled(false)
                                    .pathStyleAccessEnabled(true)
                                    .build())
                            .build();

                }).orElseGet(() -> {
                    log.info("➡️ Initialize (legacy) s3 client using AWS Credentials");
                    return S3Client.builder()
                            .region(Region.of(region))
                            .credentialsProvider(awsCredentialsProvider())
                            .crossRegionAccessEnabled(true)
                            .build();
                });
    }
}
