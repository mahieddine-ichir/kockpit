package org.kockpit.audit.notification.kinesis;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.kockpit.sdk.SdkApplicationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.kinesis.common.KinesisClientUtil;

@Configuration
@Slf4j
public class KinesisAuditServiceAutoConfiguration {

  @Bean
  AuditReportNotificationService eventHubAuditReportNotificationService(
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
          AwsCredentialsProvider awsCredentialsProvider,
          @Value("${kockpit.service.aws.region}") String regionString
  ) {
      Region region = Region.of(regionString);
      return KinesisClientUtil.createKinesisAsyncClient(
              KinesisAsyncClient.builder()
                      .credentialsProvider(awsCredentialsProvider)
                      .region(region));
  }

  @Bean
  AwsCredentialsProvider roleCredentialsProvider(
          @Value("${kockpit.service.aws.role-arn}") String roleArn,
          @Value("${kockpit.service.aws.region}") String regionString
  ) {
      String roleSessionName = "kockpitaudit-clientapp-session-" + Math.random();
      Region region = Region.of(regionString);
      AssumeRoleRequest assumeRoleRequest =
              AssumeRoleRequest.builder()
                      .roleArn(roleArn)
                      .roleSessionName(roleSessionName)
                      .durationSeconds(900)
                      .build();

      SdkHttpClient httpClient = ApacheHttpClient.builder().build();
      StsClient stsClient = StsClient.builder().region(region).httpClient(httpClient).build();
      StsAssumeRoleCredentialsProvider stsAssumeRoleCredentialsProvider =
              StsAssumeRoleCredentialsProvider.builder()
                        .stsClient(stsClient)
                        .refreshRequest(assumeRoleRequest)
                        .asyncCredentialUpdateEnabled(true)
                        .build();
        log.info("Initializing sts role credential provider: {}", stsAssumeRoleCredentialsProvider.prefetchTime());
        return stsAssumeRoleCredentialsProvider;
    }
}
