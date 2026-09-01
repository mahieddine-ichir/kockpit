package org.kockpit.audit.stream.opensearch;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpHost;
import org.kockpit.audit.api.AuditorEventService;
import org.kockpit.audit.api.AuditorKeyValueService;
import org.kockpit.audit.api.AuditorService;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.kockpit.sdk.SdkApplicationProperties;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.jackson3.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5Transport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.stream.Stream;

@AutoConfiguration
@EnableScheduling
@Slf4j
public class OpensearchAuditConsumerConfiguration {

    ObjectMapper opensearchObjectMapper() {
        // java.time et Optional sont integres a jackson-databind 3 : plus de module a enregistrer.
        // Les deux reglages de dates sont explicites parce que Jackson 3 inverse leurs defauts :
        // les documents indexes portent start/end en epoch-millis entiers.
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                // JsonGenerator.Feature a ete scinde en Jackson 3 : cette option est desormais
                // une StreamWriteFeature.
                .enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
                // Jackson 3 trie les proprietes alphabetiquement par defaut ; on conserve l'ordre
                // de declaration (defaut Jackson 2) pour ne pas changer le _source indexe.
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .build();
    }

    JsonpMapper opensearchJsonpMapper() {
        return new JacksonJsonpMapper(opensearchObjectMapper());
    }

    @Bean
    public OpensearchV3IndexManager opensearchV3IndexManager(OpenSearchClient openSearchClient) {
        return new OpensearchV3IndexManager(openSearchClient, true); // fixme through config
    }

    @Bean("opensearch")
    public AuditConsumer auditConsumer(
            OpenSearchClient openSearchClient,
            OpensearchV3IndexManager opensearchV3IndexManager,
            AuditorService auditorService, AuditorKeyValueService auditorKeyValueService, AuditorEventService auditorEventService,
            SdkApplicationProperties sdkApplicationProperties,
            @Value("${kockpit.audit.stream.opensearch.index_suffix}") String indexSuffix,
            @Value("${kockpit.audit.stream.opensearch.ttl_default_in_days}") Integer ttlDefaultInDays,
            @Value("${kockpit.audit.stream.opensearch.bulk_batch_size:100}") int bulkBatchSize
    ) {
        return new AuditConsumerForOpensearch(
                openSearchClient,
                opensearchV3IndexManager,
                auditorService, auditorKeyValueService, auditorEventService,
                sdkApplicationProperties,
                opensearchObjectMapper(),
                indexSuffix,
                ttlDefaultInDays,
                bulkBatchSize
        );
    }

    @ConditionalOnMissingBean
    @Bean
    @Lazy
    OpenSearchClient openSearchClientSecondary(
            @Value("${kockpit.audit.stream.opensearch.endpoints}") String endpoints
    ) {
        log.info("➡️ OpenSearch endpoints: {}", endpoints);

        HttpHost[] httpHosts = Stream.of(endpoints.split(","))
                .map(String::trim)
                .map(this::create)
                .toArray(HttpHost[]::new);

        ApacheHttpClient5Transport httpClient5Transport = ApacheHttpClient5TransportBuilder.builder(httpHosts)
                .setMapper(opensearchJsonpMapper())
                .build();
        return new OpenSearchClient(httpClient5Transport);
    }

    @SneakyThrows
    private HttpHost create(String endpoint) {
        return HttpHost.create(endpoint);
    }
}
