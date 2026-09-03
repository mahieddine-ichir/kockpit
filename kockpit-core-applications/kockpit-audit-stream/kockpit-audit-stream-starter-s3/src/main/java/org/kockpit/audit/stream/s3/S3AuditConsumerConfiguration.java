package org.kockpit.audit.stream.s3;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@AutoConfiguration
@EnableScheduling
@Slf4j
public class S3AuditConsumerConfiguration {

    @Bean("auditS3Client")
    S3Client auditS3Client(
            @Value("${kockpit.audit.stream.s3.endpoint:}") Optional<String> s3EndpointOptional,
            @Value("${aws.region}") String awsRegion
    ) {
        Region region = Region.of(awsRegion);
        return s3EndpointOptional
                .map(String::trim)
                .filter(StringUtils::hasLength)
                .map(s3Endpoint -> {
                    log.info("➡️ S3 (audit archive) endpoint: {}", s3Endpoint);
                    return S3Client.builder()
                            .endpointOverride(URI.create(s3Endpoint))
                            .region(region)
                            .forcePathStyle(true)
                            .build();
                }).orElseGet(() -> {
                    log.info("➡️ Initialize S3 (audit archive) client using AWS Credentials");
                    return S3Client.builder()
                            .region(region)
                            .credentialsProvider(credentialsProvider())
                            .build();
                });
    }

    AwsCredentialsProvider credentialsProvider() {
        // EC2 instance role -> InstanceProfileCredentialsProvider.builder().build()
        // ECS task role -> ContainerProvider.builder().build()
        return DefaultCredentialsProvider.builder().build();  // Auto-detects IAM role
    }

    // S3AuditConsumer deliberately does NOT implement AuditConsumer (unlike, say, a naive
    // "AuditConsumer implements..." on the class itself) - if it did, this unconditional bean
    // would be picked up directly by KockpitStreamApplication's unqualified
    // `List<AuditConsumer> consumerList` autowiring, in addition to whichever facade wraps it
    // below (or the opensearch-s3 composite), double-processing every event regardless of the
    // @ConditionalOnProperty gates on those facades - Spring's list-autowiring collects every bean
    // assignable to the type, independent of any conditional on a *different* bean that merely
    // wraps it.
    //
    // Declared with the concrete S3AuditConsumer type so other starters, e.g. opensearch-s3, can
    // inject it directly. Left unconditional (not gated on kockpit.audit.stream.consumer) because
    // opensearch-s3 needs this exact Spring-managed bean regardless of which "consumer" mode is
    // active - S3AuditConsumer.flush() relies on @Scheduled, which only fires for beans the
    // container actually manages; building a second, un-registered instance elsewhere would
    // silently never get scheduled.
    @Bean
    S3AuditConsumer s3AuditConsumer(
            @Qualifier("auditS3Client") S3Client auditS3Client,
            @Value("${kockpit.audit.stream.s3.bucket_name}") String bucketName,
            @Value("${kockpit.audit.stream.batch_size:50}") Integer batchSize,
            @Value("${kockpit.audit.stream.ttl_default_in_days:1}") Integer ttlDefaultInDays,
            @Value("${kockpit.audit.stream.s3.allowed_ttl_days:1,7,14,30,60,90,120}") List<Integer> allowedTtlDays,
            ApplicationEventPublisher eventPublisher,
            // Default (256 MiB) is a starting point, not a measured value - size it to the
            // container's heap and how much headroom the rest of the app (OpenSearch bulk
            // requests, Kinesis/KCL buffers, ...) needs alongside it.
            @Value("${kockpit.audit.stream.s3.max_buffered_bytes:268435456}") long maxBufferedBytes
    ) {
        return new S3AuditConsumer(auditS3Client, bucketName, batchSize, ttlDefaultInDays, allowedTtlDays, eventPublisher, maxBufferedBytes);
    }

    // Gated on kockpit.audit.stream.consumer=s3 so that composing this starter with others (e.g.
    // via opensearch-s3, which depends on this module for the concrete S3AuditConsumer bean above)
    // doesn't also register this facade as a second, independent AuditConsumer -
    // KockpitStreamApplication dispatches every event to every AuditConsumer bean in the context.
    @Bean("s3")
    @ConditionalOnProperty(name = "kockpit.audit.stream.consumer", havingValue = "s3")
    public AuditConsumer auditConsumerS3(S3AuditConsumer s3AuditConsumer) {
        return s3AuditConsumer::accept;
    }
}
