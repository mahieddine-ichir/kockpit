package com.accor.wcp.aws.kinesis.producer;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.kinesis.common.KinesisClientUtil;

@Slf4j
public class AbstractKinesisProducer {

  protected KinesisAsyncClient kinesisClient;

  @Value("${aws.kinesis.endpoint:}")
  private String kinesisEndpoint;

  @PostConstruct
  void init() {
    Region region = getRegion();
    String kinesisEndpoint = getKinesisEndpoint();
    if (isNotEmpty(kinesisEndpoint)) {
      // Local dev
      kinesisClient =
          KinesisClientUtil.createKinesisAsyncClient(
              KinesisAsyncClient.builder()
                  .endpointOverride(URI.create(kinesisEndpoint))
                  .httpClientBuilder(
                      NettyNioAsyncHttpClient.builder()
                          .maxConcurrency(10)
                          .maxPendingConnectionAcquires(1000))
                  .region(region));
    } else {
      AwsCredentialsProvider awsCredentialsProvider = getAwsCredentialsProvider();

      kinesisClient =
          KinesisClientUtil.createKinesisAsyncClient(
              KinesisAsyncClient.builder()
                  .credentialsProvider(awsCredentialsProvider)
                  .httpClientBuilder(
                      NettyNioAsyncHttpClient.builder()
                          .maxConcurrency(10)
                          .maxPendingConnectionAcquires(1000))
                  .region(region));
    }
  }

  protected AwsCredentialsProvider getAwsCredentialsProvider() {
    return DefaultCredentialsProvider.create();
  }

  protected String getKinesisEndpoint() {
    return kinesisEndpoint;
  }

  protected Region getRegion() {
    return Region.EU_WEST_1;
  }

  public void setKinesisClient(KinesisAsyncClient kinesisClient) {
    this.kinesisClient = kinesisClient;
  }
}
