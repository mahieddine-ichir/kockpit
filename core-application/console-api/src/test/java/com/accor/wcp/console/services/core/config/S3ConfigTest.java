package com.accor.wcp.console.services.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;

class S3ConfigTest {

  @Test
  public void should_init_s3_bean() {
    String dynamoDbEndpoint = "http://localhost";
    S3Config s3Config = new S3Config(dynamoDbEndpoint);

    S3Client amazonS3 = s3Config.amazonS3();

    assertThat(amazonS3).isNotNull();
  }
}
