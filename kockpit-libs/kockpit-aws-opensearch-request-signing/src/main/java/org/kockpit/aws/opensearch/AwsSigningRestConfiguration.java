package org.kockpit.aws.opensearch;

import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;

@AutoConfiguration
@Slf4j
class AwsSigningRestConfiguration {

  @Bean
  @Primary
  OpenSearchClient openSearchClient(
          @Value("${aws.region}") String region,
          @Value("${kockpit.audit.stream.opensearch.endpoints}") String endpoint,
          SdkHttpClient sdkHttpClient) {

      log.info("➡️ OpenSearch endpoint: {}", endpoint);
      return new OpenSearchClient(
              new AwsSdk2Transport(
                      sdkHttpClient,
                      endpoint,
                      Region.of(region),
                      AwsSdk2TransportOptions.builder().build()
              )
      );
  }

  @Bean
  SdkHttpClient sdkHttpClient() {
    SdkHttpClient build = ApacheHttpClient.builder().build();
    Runtime.getRuntime().addShutdownHook(new Thread(build::close));
    return build;
  }
}
