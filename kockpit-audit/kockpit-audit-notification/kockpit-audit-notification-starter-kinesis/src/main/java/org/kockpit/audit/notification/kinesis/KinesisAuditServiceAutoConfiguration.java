package org.kockpit.audit.notification.kinesis;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.kockpit.sdk.SdkApplicationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;

import java.net.URI;

@Configuration
@Slf4j
public class KinesisAuditServiceAutoConfiguration {

  @Bean
  AuditReportNotificationService kinesisAuditReportNotificationService(
          SdkApplicationProperties sdkApplicationProperties,
          RecordPartitioner recordPartitioner,
          RecordTransformer recordTransformer,
          KinesisAsyncClient kinesisClient
  ) {
      String streamName = "auditstream-" + sdkApplicationProperties.getEnv();
      return new KinesisAuditReportNotificationService(
              kinesisClient, streamName, recordPartitioner, recordTransformer
      );
  }

  @Bean
  RecordPartitioner recordPartitioner() {
      return new RecordPartitioner();
  }

  @ConditionalOnMissingBean(RecordTransformer.class)
  @Bean
  RecordTransformer identityTransformer(RecordPartitioner recordPartitioner) {
      return auditJsonReport -> {
          String partitionKey = recordPartitioner.computePartitionKey(auditJsonReport);
          return PutRecordsRequestEntry.builder()
                  .partitionKey(partitionKey)
                  .data(SdkBytes.fromByteArray(auditJsonReport.getAuditJson().getBytes()))
                  .build();
      };
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
