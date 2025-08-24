package com.example.insightsstreamingapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class OpenSearchAutoConfiguration {

    @Bean
    public RestHighLevelClient restHighLevelClient(
            @Value("${opensearch.host:localhost}") String host,
            @Value("${opensearch.port:9200}") int port) {
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(host, port, "http"))
        );
    }

    @Bean("opensearch-insights")
    public AuditConsumer insightsConsumer(
            RestHighLevelClient client,
            @Value("${opensearch.insights_index:insightsss}") String index,
            ObjectMapper objectMapper) {
        return new OpenSearchInsightsConsumer(client, index, objectMapper);
    }


}