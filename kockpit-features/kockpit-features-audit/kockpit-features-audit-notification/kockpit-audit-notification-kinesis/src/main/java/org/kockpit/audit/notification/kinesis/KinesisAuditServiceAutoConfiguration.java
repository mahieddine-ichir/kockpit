package org.kockpit.audit.notification.kinesis;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReportNotificationService;
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
          KinesisAsyncClient kinesisClient,
          @Value("${kockpit.audit.notification.kinesis.stream_name}") String streamName
  ) {
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

  @Bean
  KinesisAsyncClient kinesisAsyncClient(
          @Value("${aws.region}") String regionString,
          @Value("${kockpit.audit.notification.kinesis.endpoint:}") String kinesisEndpoint
  ) {
      Region region = Region.of(regionString);
      if (kinesisEndpoint == null || kinesisEndpoint.isEmpty()) {
          return KinesisAsyncClient.builder()
                  .credentialsProvider(credentialsProvider())
                  .region(region)
                  .build();
      } else {
          return KinesisAsyncClient.builder()
                  .endpointOverride(URI.create(kinesisEndpoint))
                  .region(region)
                  .build();
      }
  }

  AwsCredentialsProvider credentialsProvider() {
      return DefaultCredentialsProvider.builder().build();
  }
}
