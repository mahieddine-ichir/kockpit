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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@AutoConfiguration
@Slf4j
public class OpensearchAutoConfiguration {

    @Bean
    OpensearchRepository opensearchRepository(
            RestHighLevelClient restHighLevelClient,
            @Value("${kockpit.backend.opensearch.index}") String index,
            AuditArchiveReader auditArchiveReader,
            // Must mirror kockpit.audit.stream.opensearch.wrap_indexed_key_values on the writer
            // side (OpensearchIndexer) for whichever environment this points at - the nested
            // query path has to match the index's actual mapping, not just its own default.
            @Value("${kockpit.backend.opensearch.wrap_indexed_key_values:false}") boolean wrapIndexedKeyValues
    ) {
        log.info(
"""
    \n
    - Opensearch index: {}
""", index);

        return new OpensearchRepository(restHighLevelClient, index, auditArchiveReader, wrapIndexedKeyValues);
    }

    @Bean
    AuditArchiveReader auditArchiveReader(S3Client s3Client, @Qualifier("opensearch-objectMapper") ObjectMapper objectMapper) {
        return new AuditArchiveReader(s3Client, objectMapper);
    }

    // Same property namespace as the manifests' S3 backend (kockpit.aws.region /
    // kockpit.aws.s3.endpoint) - one S3 client config for the whole backend-application, not a
    // second one specific to this module. @ConditionalOnMissingBean reuses that module's bean
    // when it's also on the classpath (S3-backed manifests) instead of creating a duplicate.
    @Bean
    @ConditionalOnMissingBean(S3Client.class)
    S3Client s3Client(
            @Value("${kockpit.aws.region:eu-west-1}") String region,
            @Value("${kockpit.aws.s3.endpoint:}") Optional<String> optionalEndpoint
    ) {
        return optionalEndpoint
                .map(String::trim)
                .filter(StringUtils::hasLength)
                .map(endpoint -> {
                    log.info("➡️ s3 endpoint: {}", endpoint);
                    return S3Client.builder()
                            .region(Region.of(region))
                            .endpointOverride(URI.create(endpoint))
                            .crossRegionAccessEnabled(true)
                            .serviceConfiguration(S3Configuration.builder()
                                    .chunkedEncodingEnabled(false)
                                    .pathStyleAccessEnabled(true)
                                    .build())
                            .build();
                }).orElseGet(() -> {
                    log.info("➡️ Initialize s3 client using AWS Credentials");
                    return S3Client.builder()
                            .region(Region.of(region))
                            .credentialsProvider(DefaultCredentialsProvider.builder().build())
                            .crossRegionAccessEnabled(true)
                            .build();
                });
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
