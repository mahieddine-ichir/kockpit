package com.example.kockpitbackendserviceopensearch;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;
import org.kockpit.audit.backend.ConfigApiDelegate;
import org.kockpit.audit.backend.DomainApiDelegate;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import javax.net.ssl.SSLContext;

@AutoConfiguration
public class OpenSearchAutoConfiguration {

    @Bean
    DomainApiDelegate openSearchRepository(OpenSearchClient client,
                                           @Value("${kockpit.audit.opensearch.index}") String indexName) {
        return new OpenSearchRepository(client, indexName);
    }

    @Bean
    ConfigApiDelegate localFileConfigRepository() {
        return new LocalFileConfigRepository();
    }

    @Bean
    OpenSearchClient openSearchClient(
            @Value("${kockpit.audit.opensearch.host}") String host,
            @Value("${kockpit.audit.opensearch.port:9200}") int port,
            @Value("${kockpit.audit.opensearch.scheme:http}") String scheme,
            @Value("${kockpit.audit.opensearch.username:#{null}}") String username,
            @Value("${kockpit.audit.opensearch.password:#{null}}") String password) {

        HttpHost httpHost = new HttpHost(host, port, scheme);

        RestClientBuilder builder = RestClient.builder(httpHost);

        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            try {
                SSLContext sslContext = SSLContexts.custom()
                        .loadTrustMaterial((chain, authType) -> true)
                        .build();

                httpClientBuilder.setSSLContext(sslContext);

                if (username != null && password != null) {
                    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(AuthScope.ANY,
                            new UsernamePasswordCredentials(username, password));
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
            } catch (Exception e) {
                throw new RuntimeException("failed credi", e);
            }
            return httpClientBuilder;
        });
        RestClient restClient = builder.build();
        OpenSearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        return new OpenSearchClient(transport);
    }
}