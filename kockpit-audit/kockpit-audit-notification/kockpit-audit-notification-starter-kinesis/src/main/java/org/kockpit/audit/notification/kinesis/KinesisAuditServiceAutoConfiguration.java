package org.kockpit.audit.notification.kinesis;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.kinesis.common.KinesisClientUtil;

@Configuration
@Slf4j
public class KinesisAuditServiceAutoConfiguration {

  @Bean
  AuditReportNotificationService eventHubAuditReportNotificationService(
          KinesisAsyncClient kinesisClient,
          @Value("${kockpit.sdk.service.audit.notification.topic}") String topic) {
    return new KinesisAuditReportNotificationService(kinesisClient);
  }

  @Bean
  KinesisAsyncClient kinesisAsyncClient(
          @Value("${}") String region
  ) {
      String roleSessionName = "wcpaudit-clientapp-session-" + Math.random();
      AwsCredentialsProvider stsProvider =
              roleCredentialsProvider(roleArn, roleSessionName, region);

      kinesisClient =
              KinesisClientUtil.createKinesisAsyncClient(
                      KinesisAsyncClient.builder()
                              .credentialsProvider(stsProvider)
                              //
                              // .httpClientBuilder(NettyNioAsyncHttpClient.builder()
                              //                                      .maxConcurrency(10)
                              //                                      .maxPendingConnectionAcquires(1000))
                              .region(region));
  }

  @Bean
  AwsCredentialsProvider roleCredentialsProvider(
          String roleArn, String roleSessionName, Region region) {

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
        log.info(
                "Initializing sts role credential provider: "
                        + stsAssumeRoleCredentialsProvider.prefetchTime().toString());
        return stsAssumeRoleCredentialsProvider;
    }
}
