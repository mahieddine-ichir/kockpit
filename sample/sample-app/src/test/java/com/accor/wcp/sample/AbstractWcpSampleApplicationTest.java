package com.accor.wcp.sample;

import com.accor.wcp.sample.AbstractWcpSampleApplicationTest.TestConfiguration;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = WcpSampleApplicationApp.class)
@AutoConfigureMockMvc
@ActiveProfiles("integrationtest")
@Import(TestConfiguration.class)
public abstract class AbstractWcpSampleApplicationTest {

  // bind the above RANDOM_PORT
  @LocalServerPort protected int port;

  @Autowired protected TestRestTemplate testRestTemplate;

  @Autowired protected KinesisClient kinesisClient;

  @Autowired protected SqsClient sqsClient;

  @Autowired protected SqsClient sqsdlqClient;

  @Configuration
  static class TestConfiguration {
    @Bean
    KinesisClient kinesisClient() {
      return Mockito.mock(KinesisClient.class);
    }

    @Bean
    SqsClient sqsdlqClient() {
      return Mockito.mock(SqsClient.class);
    }

    @Bean
    SqsClient sqsClient() {
      return Mockito.mock(SqsClient.class);
    }
  }
}
