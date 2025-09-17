package org.kockpit.audit.stream.opensearch;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.CollectionUtils;

import java.util.List;

@AutoConfiguration
@EnableScheduling
public class OpensearchAuditConsumerConfiguration {

    @Bean("opensearch")
    public AuditConsumer auditConsumer(
            RestHighLevelClient restHighLevelClient
    ) {
        return new OpensearchIndexer(restHighLevelClient,
                new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .registerModule(new JavaTimeModule())
        );
    }

    @Bean
    RestHighLevelClient azureSearchIndexer(
            @Value("${kockpit.audit.stream.opensearch.endpoint}") String endpoint,
            @Autowired(required = false) List<HttpRequestInterceptor> interceptors
    ) {
        RestClientBuilder builder = RestClient.builder(HttpHost.create(endpoint));
        if (! CollectionUtils.isEmpty(interceptors)) {
            interceptors.forEach(interceptor -> builder.setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
        }
        return new RestHighLevelClient(builder);
    }
}
