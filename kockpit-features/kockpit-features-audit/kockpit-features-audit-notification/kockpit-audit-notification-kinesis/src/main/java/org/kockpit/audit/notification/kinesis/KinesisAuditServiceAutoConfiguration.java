package org.kockpit.audit.notification.kinesis;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.kockpit.audit.api.CompressionService;
import org.kockpit.sdk.SdkApplicationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;

import java.net.URI;

@Configuration
@Slf4j
public class KinesisAuditServiceAutoConfiguration {

  @Bean
  CompressionService compressionService(
          @Value("${kockpit.audit.compression.enabled:true}") boolean compressionEnabled) {
    return new CompressionService(compressionEnabled);
  }

  @Bean
  AuditReportNotificationService kinesisAuditReportNotificationService(
          SdkApplicationProperties sdkApplicationProperties,
          RecordTransformer recordTransformer,
          KinesisAsyncClient kinesisClient
  ) {
      String streamName = "auditstream-" + sdkApplicationProperties.getEnv();
      return new KinesisAuditReportNotificationService(kinesisClient, streamName, recordTransformer);
  }

  @ConditionalOnMissingBean(DefaultRecordPartitioner.class)
  @Bean
  RecordPartitioner defaultRecordPartitioner() {
      return new DefaultRecordPartitioner(256);
  }

  @ConditionalOnMissingBean(RecordTransformer.class)
  @Bean
  RecordTransformer defaultRecordTransformer(RecordPartitioner recordPartitioner, CompressionService compressionService) {
      return new DefaultRecordTransformer(recordPartitioner, compressionService);
  }

  @Bean
  KinesisAsyncClient kinesisAsyncClient(
          @Value("${kockpit.service.aws.region}") String regionString,
          @Value("${kockpit.audit.notification.kinesis.endpoint:http://localhost:4566}") String kinesisEndpoint,
          AwsCredentialsProvider awsCredentialsProvider
  ) {
      Region region = Region.of(regionString);
      return KinesisAsyncClient.builder()
              .endpointOverride(URI.create(kinesisEndpoint))
              .region(region)
              .credentialsProvider(awsCredentialsProvider)
              .build();
  }

  @Bean
  AwsCredentialsProvider roleCredentialsProvider() {
      return DefaultCredentialsProvider.builder().build();
  }
}
