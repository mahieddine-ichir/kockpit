package org.kockpit.ai.mcp.server.aws;

import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.auth.aws.internal.signer.DefaultAwsV4HttpSigner;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;

@AutoConfiguration
public class AwsRestClientInterceptorConfig {

  @Bean
  HttpRequestInterceptor awsHttpRequestInterceptor(RequestSigner requestSigner) {
    return new AwsRequestSigningApacheInterceptor(requestSigner);
  }

  @Bean
  AwsCredentialsProvider credentialsProvider() {
    return DefaultCredentialsProvider.create();
  }

  @Bean
  RequestSigner awsRequestSigner(
          @Value("${aws.service.name}") String serviceName,
          @Value("${aws.region}") String region,
          AwsCredentialsProvider credentialsProvider) {
    AwsV4HttpSigner signer = new DefaultAwsV4HttpSigner();
    return new RequestSigner(signer, credentialsProvider, serviceName, software.amazon.awssdk.regions.Region.of(region));
  }
}
