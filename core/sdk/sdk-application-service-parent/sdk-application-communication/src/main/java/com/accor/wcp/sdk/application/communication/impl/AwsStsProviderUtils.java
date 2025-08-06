package com.accor.wcp.sdk.application.communication.impl;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

/** Internal helper to manage AWS STS Credential Provider. */
@Slf4j
final class AwsStsProviderUtils {
  private AwsStsProviderUtils() {}

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
}
