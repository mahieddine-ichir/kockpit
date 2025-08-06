package com.accor.wcp.audit.notification.autoconfigure.kinesis;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.accor.wcp.audit.notification.kinesis.KinesisAuditReportNotificationService;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.kinesis.common.KinesisClientUtil;

@Slf4j
@RequiredArgsConstructor
public class KinesisAuditNotificationServiceConfiguration {

  private final String streamName;

  private final String roleArn;

  private final String kinesisEndpoint;

  private KinesisAsyncClient kinesisClient;

  static AwsCredentialsProvider roleCredentialsProvider(
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

  @Bean
  public KinesisAuditReportNotificationService kinesisAuditNotificationService() {
    Region region = Region.EU_WEST_1;
    if (isNotEmpty(kinesisEndpoint)) {
      kinesisClient =
          KinesisClientUtil.createKinesisAsyncClient(
              KinesisAsyncClient.builder()
                  .endpointOverride(URI.create(kinesisEndpoint))
                  //
                  // .httpClientBuilder(NettyNioAsyncHttpClient.builder()
                  //                                      .maxConcurrency(10)
                  //                                      .maxPendingConnectionAcquires(1000))
                  .region(region));
    } else {
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

    return new KinesisAuditReportNotificationService(kinesisClient, streamName);
  }
}
