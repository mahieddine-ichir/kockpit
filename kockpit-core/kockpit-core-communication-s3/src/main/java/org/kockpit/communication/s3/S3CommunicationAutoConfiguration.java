package org.kockpit.communication.s3;

import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Consumer;
import org.kockpit.communication.Publisher;
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
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.util.Optional;

@AutoConfiguration
@ConditionalOnProperty(
        value = "kockpit.communication.s3.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Slf4j
public class S3CommunicationAutoConfiguration {

    @Bean
    Publisher s3Publisher(
            S3Client s3Client,
            @Value("${kockpit.aws.s3.bucket}") String bucketName
    ) {
        return new S3Publisher(s3Client, bucketName, objectMapper());
    }

    @Bean
    Consumer s3Consumer(
            S3Client s3Client,
            @Value("${kockpit.aws.s3.bucket}") String bucketName
    ) {
        return new S3Consumer(s3Client, bucketName, objectMapper());
    }

    ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
                // Jackson 3 trie les proprietes alphabetiquement par defaut ; on conserve l'ordre
                // de declaration (defaut Jackson 2) pour ne pas changer le JSON produit.
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .build();
    }

    AwsCredentialsProvider awsCredentialsProvider() {
        return DefaultCredentialsProvider.builder().build();
    }

    @ConditionalOnMissingBean(S3Client.class)
    @Bean
    S3Client s3Client(
            @Value("${kockpit.aws.region}") String region,
            @Value("${kockpit.communication.s3.endpoint:}") Optional<String> optionalEndpoint
    ) {
        return optionalEndpoint
                .map(String::trim)
                .filter(StringUtils::hasLength)
                .map(endpoint -> {
                    log.info("➡️ s3 endpoint: {}", endpoint);
                    return S3Client.builder()
                            .region(Region.of(region))
                            .endpointOverride(URI.create(endpoint))
                            .serviceConfiguration(S3Configuration.builder()
                                    .chunkedEncodingEnabled(false)
                                    .pathStyleAccessEnabled(true)
                                    .build())
                            .build();

                }).orElseGet(() -> {
                    log.info("➡️ Initialize s3 client using AWS Credentials");
                    return S3Client.builder()
                            .region(Region.of(region))
                            .credentialsProvider(awsCredentialsProvider())
                            .crossRegionAccessEnabled(true)
                            .build();
                });
    }
}
