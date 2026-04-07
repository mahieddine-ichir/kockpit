package org.kockpit.aws.opensearch;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
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
          @Value("${kockpit.aws.region}") String region,
          @Value("${kockpit.audit.stream.opensearch.endpoints:}") String endpoint,
          SdkHttpClient sdkHttpClient) {

      log.info("➡️ OpenSearch endpoint (AWS): {}", endpoint);
      return new OpenSearchClient(
              new AwsSdk2Transport(
                      sdkHttpClient,
                      endpoint,
                      Region.of(region),
                      AwsSdk2TransportOptions.builder().setMapper(
                              opensearchJsonpMapper()
                      ).build()
              )
      );
  }

    JsonpMapper opensearchJsonpMapper() {
        return new JacksonJsonpMapper(opensearchObjectMapper());
    }

    ObjectMapper opensearchObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
                .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
                // fixme .configure(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN, true);
    }

    @Bean
    SdkHttpClient sdkHttpClient() {
        SdkHttpClient build = ApacheHttpClient.builder().build();
        Runtime.getRuntime().addShutdownHook(new Thread(build::close));
        return build;
    }
}
