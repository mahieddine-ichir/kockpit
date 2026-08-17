package org.kockpit.aws.opensearch;

import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.jackson3.JacksonJsonpMapper;
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
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

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
        // java.time et Optional sont integres a jackson-databind 3 : plus de module a enregistrer.
        // Les deux reglages de dates sont explicites parce que Jackson 3 inverse leurs defauts :
        // les documents indexes portent start/end en epoch-millis entiers.
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                // Aligne sur OpensearchAuditConsumerConfiguration, qui indexe les memes
                // documents : sans ce reglage les deux chemins ecrivent les BigDecimal
                // differemment (notation decimale ici, scientifique la-bas).
                .enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
                // Jackson 3 trie les proprietes alphabetiquement par defaut ; on conserve l'ordre
                // de declaration (defaut Jackson 2) pour ne pas changer le _source indexe.
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .build();
    }

    @Bean
    SdkHttpClient sdkHttpClient() {
        SdkHttpClient build = ApacheHttpClient.builder().build();
        Runtime.getRuntime().addShutdownHook(new Thread(build::close));
        return build;
    }
}
