package org.kockpit.audit.stream.opensearch;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpHost;
import org.kockpit.audit.api.AuditorEventService;
import org.kockpit.audit.api.AuditorKeyValueService;
import org.kockpit.audit.api.AuditorService;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.kockpit.sdk.SdkApplicationProperties;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5Transport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.stream.Stream;

@AutoConfiguration
@EnableScheduling
@Slf4j
public class OpensearchAuditConsumerConfiguration {

    ObjectMapper opensearchObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .registerModule(new JavaTimeModule())
                .registerModule(new Jdk8Module())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
                .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
                .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
    }

    JsonpMapper opensearchJsonpMapper() {
        return new JacksonJsonpMapper(opensearchObjectMapper());
    }

    @Bean
    public OpensearchV3IndexManager opensearchV3IndexManager(OpenSearchClient openSearchClient) {
        return new OpensearchV3IndexManager(openSearchClient);
    }

    @Bean("opensearch")
    public AuditConsumer auditConsumer(
            OpenSearchClient openSearchClient,
            OpensearchV3IndexManager opensearchV3IndexManager,
            AuditorService auditorService, AuditorKeyValueService auditorKeyValueService, AuditorEventService auditorEventService,
            SdkApplicationProperties sdkApplicationProperties,
            @Value("${kockpit.audit.stream.opensearch.index_suffix}") String indexSuffix,
            @Value("${kockpit.audit.stream.opensearch.ttl_default_in_days}") Integer ttlDefaultInDays
    ) {
        return new AuditConsumerForOpensearch(
                openSearchClient,
                opensearchV3IndexManager,
                auditorService, auditorKeyValueService, auditorEventService,
                sdkApplicationProperties,
                opensearchObjectMapper(),
                indexSuffix,
                ttlDefaultInDays
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
