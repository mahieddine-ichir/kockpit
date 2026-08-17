package org.kockpit.backend.services.search.opensearch;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.util.TimeValue;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@AutoConfiguration
@Slf4j
public class OpensearchAutoConfiguration {

    @Bean
    OpensearchRepository opensearchRepository(
            RestHighLevelClient restHighLevelClient,
            @Value("${kockpit.backend.opensearch.index}") String index
    ) {
        log.info(
"""
    \n
    - Opensearch index: {}
""", index);

        return new OpensearchRepository(restHighLevelClient, index);
    }

    // Bean non primaire : le mapper auto-configure par Boot reste celui injecte par type
    // (JacksonAutoConfiguration l'expose en @Primary). java.time est integre a databind 3.
    @Bean("opensearch-objectMapper")
    public ObjectMapper opensearchObjectMapper() {
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    @Bean
    RestClient restClient(RestClientBuilder builder) {
        return builder.build();
    }

    @SneakyThrows
    @Bean
    RestHighLevelClient restHighLevelClient(RestClientBuilder builder) {
        return new RestHighLevelClient(builder);
    }

    @SneakyThrows
    @Bean
    RestClientBuilder osRestClientBuilder(
            @Value("${kockpit.backend.opensearch.endpoints}") String endpoints,
            @Autowired(required = false) List<HttpRequestInterceptor> interceptors
    ) {
        HttpHost[] httpHosts = Arrays.stream(endpoints.split(","))
                .map(s -> {
                    try {
                        return HttpHost.create(s);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toArray(HttpHost[]::new);
        log.info(
                """
                    \n
                    - Opensearch endpoints list: {}
                """, endpoints);

        RestClientBuilder builder = RestClient.builder(httpHosts);
        builder.setHttpClientConfigCallback(hacb -> {
            hacb.evictExpiredConnections();
            hacb.evictIdleConnections(TimeValue.of(30, TimeUnit.SECONDS));
            if (!CollectionUtils.isEmpty(interceptors)) {
                interceptors.forEach(hacb::addRequestInterceptorLast);
            }
            return hacb;
        });
        return builder;
    }
}
