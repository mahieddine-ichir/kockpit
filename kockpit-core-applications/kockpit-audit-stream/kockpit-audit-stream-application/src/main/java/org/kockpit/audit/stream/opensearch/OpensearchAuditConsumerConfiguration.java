package org.kockpit.audit.stream.opensearch;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

@AutoConfiguration
@EnableScheduling
@Slf4j
public class OpensearchAuditConsumerConfiguration {

    @Bean("opensearch-object-mapper")
    public ObjectMapper opensearchObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule());
    }

    @Bean("opensearch-index-manager")
    public IndexManager opensearchIndexManager(
            RestHighLevelClient restHighLevelClient,
            @Qualifier("opensearch-object-mapper") ObjectMapper objectMapper,
            @Value("${kockpit.audit.stream.opensearch.index_suffix}") String indexSuffix
            ) {
        log.info("""
        \n
            - OpenSearch index suffix: {}
        """, indexSuffix);
        return new OpensearchIndexManager(restHighLevelClient, objectMapper, indexSuffix);
    }

    @Bean("opensearch")
    public AuditConsumer auditConsumer(
            RestHighLevelClient restHighLevelClient,
            AuditReportMapper auditReportMapper,
            @Qualifier("opensearch-object-mapper") ObjectMapper objectMapper,
            @Qualifier("opensearch-index-manager") IndexManager indexManager
    ) {
        return new OpensearchIndexer(
                restHighLevelClient,
                auditReportMapper,
                objectMapper,
                indexManager
        );
    }

    @Bean
    RestHighLevelClient azureSearchIndexer(
            @Value("${kockpit.audit.stream.opensearch.endpoints}") String endpoints,
            @Autowired(required = false) List<HttpRequestInterceptor> interceptors
    ) {
        HttpHost[] httpHosts = Arrays.stream(endpoints.split(","))
                .map(HttpHost::create)
                .toArray(HttpHost[]::new);
        RestClientBuilder builder = RestClient.builder(httpHosts);
        if (! CollectionUtils.isEmpty(interceptors)) {
            interceptors.forEach(interceptor -> builder.setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
        }

        log.info("""
        \n
            - OpenSearch endpoints: {}
        """, endpoints);
        return new RestHighLevelClient(builder);
    }
}
