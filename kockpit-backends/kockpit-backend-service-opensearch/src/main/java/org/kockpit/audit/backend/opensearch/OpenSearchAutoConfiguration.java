package org.kockpit.audit.backend.opensearch;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

@AutoConfiguration
public class OpenSearchAutoConfiguration {

    @Bean
    OpensearchRepository opensearchRepository(
            RestHighLevelClient restHighLevelClient,
            @Value("${kockpit.audit.opensearch.index}") String index
    ) {
        return new OpensearchRepository(restHighLevelClient, index);
    }

    @Bean("opensearch-objectMapper")
    public ObjectMapper opensearchObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule());
    }

    @Bean
    RestHighLevelClient restHighLevelClient(
            @Value("${kockpit.audit.opensearch.endpoints}") String endpoints,
            @Autowired(required = false) List<HttpRequestInterceptor> interceptors
    ) {
        HttpHost[] httpHosts = Arrays.stream(endpoints.split(","))
                .map(HttpHost::create)
                .toArray(HttpHost[]::new);
        RestClientBuilder builder = RestClient.builder(httpHosts);
        if (! CollectionUtils.isEmpty(interceptors)) {
            interceptors.forEach(interceptor -> builder.setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
        }
        return new RestHighLevelClient(builder);
    }
}
