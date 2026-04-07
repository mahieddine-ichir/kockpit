package org.kockpit.audit.notification.kinesis;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;

import java.net.URI;
import java.util.Optional;

@AutoConfiguration
@ConditionalOnProperty(
        value = "kockpit.audit.kinesis.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Slf4j
public class KinesisAuditServiceAutoConfiguration {

  @Bean
  AuditReportNotificationService kinesisAuditReportNotificationService(
          RecordTransformer recordTransformer,
          @Qualifier("kinesisProducerClient") KinesisAsyncClient kinesisClient,
          @Value("${kockpit.audit.notification.kinesis.stream_name}") String streamName
  ) {
      log.info("➡️ Kinesis stream name: {}", streamName);
      return new KinesisAuditReportNotificationService(kinesisClient, streamName, recordTransformer);
  }

  @ConditionalOnMissingBean(RecordPartitioner.class)
  @Bean
  RecordPartitioner defaultRecordPartitioner() {
      return new DefaultRecordPartitioner(256);
  }

  @ConditionalOnMissingBean(RecordTransformer.class)
  @Bean
  RecordTransformer defaultRecordTransformer(RecordPartitioner recordPartitioner) {
      return new DefaultRecordTransformer(recordPartitioner);
  }

  @Bean("kinesisProducerClient")
  KinesisAsyncClient kinesisAsyncClient(
          @Value("${kockpit.aws.region}") String regionString,
          @Value("${kockpit.audit.notification.kinesis.endpoint:}") Optional<String> kinesisEndpoint
  ) {
      Region region = Region.of(regionString);
      return kinesisEndpoint
              .map(String::trim)
              .filter(StringUtils::isNotEmpty)
              .map(s -> {
                  log.info("➡️ Kinesis endpoint: {}", s);
                  return KinesisAsyncClient.builder()
                          .endpointOverride(URI.create(s))
                          .region(region)
                          .credentialsProvider(credentialsProvider())
                          .build();
              }).orElseGet(() -> {
                  log.info("➡️ Initialize Kinesis client using AWS Credentials");
                  return KinesisAsyncClient.builder()
                          .credentialsProvider(credentialsProvider())
                          .region(region)
                          .build();
      });
  }

  AwsCredentialsProvider credentialsProvider() {
      return DefaultCredentialsProvider.builder().build();
  }
}
