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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
        // Le reglage de date est explicite (bien qu'il corresponde au defaut Jackson 3) pour que
        // ce format reste stable si ce defaut change un jour : start/end sont ecrits en ISO-8601
        // avec la precision nanoseconde native d'Instant (ex. "2026-08-31T15:10:06.678545176Z"),
        // coherent avec les documents deja indexes par d'autres versions/services de ce flux.
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
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
    public OpensearchV3IndexManager opensearchV3IndexManager(
            OpenSearchClient openSearchClient,
            @Value("${kockpit.audit.stream.opensearch.strict_mode:true}") Boolean strictMode,
            @Value("${kockpit.audit.stream.opensearch.template_file:/opensearch/audit_index_template.json}") String templateFile,
            @Value("${kockpit.audit.stream.opensearch.policy_file:/opensearch/audit_ism_policy.json}") String policyFile
    ) {
        return new OpensearchV3IndexManager(openSearchClient, strictMode, templateFile, policyFile);
    }

    // Gated on kockpit.audit.stream.consumer=opensearch: composing this starter with others (e.g.
    // via opensearch-s3, which depends on this module for the concrete OpensearchIndexer bean
    // built below) shouldn't also register this facade as a second, independent AuditConsumer -
    // KockpitStreamApplication dispatches every event to every AuditConsumer bean in the context.
    @Bean("opensearch")
    @ConditionalOnProperty(name = "kockpit.audit.stream.consumer", havingValue = "opensearch")
    public AuditConsumer auditConsumer(
            OpensearchIndexer opensearchIndexer,
            AuditorService auditorService,
            AuditorKeyValueService auditorKeyValueService, AuditorEventService auditorEventService,
            SdkApplicationProperties sdkApplicationProperties,
            @Value("${kockpit.audit.stream.opensearch.ttl_default_in_days}") Integer ttlDefaultInDays,
            @Value("${kockpit.audit.stream.opensearch.batch_size:100}") int batchSize
    ) {
        return new AuditConsumerForOpensearch(
                opensearchIndexer,
                auditorService, auditorKeyValueService, auditorEventService,
                sdkApplicationProperties,
                ttlDefaultInDays,
                batchSize
        );
    }

    @Bean
    OpensearchIndexer opensearchIndexer(
            OpenSearchClient openSearchClient,
            OpensearchV3IndexManager opensearchV3IndexManager,
            @Value("${kockpit.audit.stream.opensearch.index_suffix}") String indexSuffix,
            @Value("${kockpit.audit.stream.opensearch.wrap_indexed_key_values:false}") boolean wrapIndexedKeyValues
    ) {
        return new OpensearchIndexer(
                openSearchClient, opensearchV3IndexManager,
                opensearchObjectMapper(),
                indexSuffix,
                wrapIndexedKeyValues
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
