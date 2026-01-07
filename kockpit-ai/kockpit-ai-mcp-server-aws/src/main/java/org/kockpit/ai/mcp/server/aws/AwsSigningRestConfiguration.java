package org.kockpit.ai.mcp.server.aws;

import org.apache.http.HttpRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.regions.Region;

@AutoConfiguration
class AwsSigningRestConfiguration {

  @Bean
  HttpRequestInterceptor awsSigningRequestInterceptor(
          @Value("${aws.service.name}") String serviceName,
          @Value("${aws.region}") String region
  ) {
    DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
    Aws4Signer signer = Aws4Signer.create();

    RequestSigner requestSigner = new RequestSigner(serviceName, signer, credentialsProvider, Region.of(region));
    return new AwsRequestSigningApacheInterceptor(requestSigner);
  }
}
