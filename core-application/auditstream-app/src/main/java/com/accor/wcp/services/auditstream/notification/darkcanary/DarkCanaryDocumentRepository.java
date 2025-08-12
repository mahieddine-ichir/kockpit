package com.accor.wcp.services.auditstream.notification.darkcanary;

import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryConfiguration;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.ConfiguredDarkCanaryIndexDocument;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.ConfiguredDarkCanaryIndexDocumentByDifference;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.DarkCanaryIndexDocument;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.DarkCanaryIndexDocumentByDifference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.DocWriteRequest;
import org.opensearch.action.bulk.BulkItemResponse;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.update.UpdateRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class DarkCanaryDocumentRepository {

    private final RestHighLevelClient restHighLevelClient;

    private ObjectMapper objectMapper;

    private final DarkCanaryIndexManager darkCanaryIndexManager;

    @Value("${darkcanarytesting.defaults.max-ttl-days}")
    private Integer maxTllDays;

    @PostConstruct
    void init() {
        objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .registerModule(new JavaTimeModule());
    }

    @SneakyThrows
    public Optional<DarkCanaryIndexDocument> find(DarkCanaryIndexDocument darkCanaryIndexDocument, DarkCanaryConfiguration darkCanaryConfiguration) {
        Integer ttl = Optional.ofNullable(darkCanaryConfiguration.getTtl())
                .orElse(maxTllDays);

        String aliasRead = darkCanaryIndexManager.getAliasRead(darkCanaryIndexDocument.getDomain(), darkCanaryIndexDocument.getEnv(), ttl);

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        darkCanaryIndexDocument.getId().forEach(keyValuePair ->
                queryBuilder.must(QueryBuilders.matchQuery("id.key", keyValuePair.getKey()))
                        .must(QueryBuilders.matchQuery("id.value", keyValuePair.getValue()))
        );

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(queryBuilder);

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(aliasRead);
        searchRequest.source(builder);

        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        if (search.getHits().getHits() == null || search.getHits().getHits().length == 0) {
            return Optional.empty();
        } else {
            // get the highest score result
            SearchHit hit = search.getHits().getHits()[0];
            DarkCanaryIndexDocument value = objectMapper.readValue(hit.getSourceAsString(), DarkCanaryIndexDocument.class);
            value.set_id(hit.getId());
            return Optional.of(value);
        }
    }

    @SneakyThrows
    public void save(BulkRequest request) {
        if (request.requests().isEmpty()) {
            log.trace("No requests found to save (empty)");
            return;
        }
        BulkResponse bulkItemResponses = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        if (bulkItemResponses.hasFailures()) {
            Stream.of(bulkItemResponses.getItems())
                    .filter(BulkItemResponse::isFailed)
                    .forEach(bulkItemResponse -> log.error("Bulk item in error {}", bulkItemResponse.getFailureMessage()));
        }
    }

    @SneakyThrows
    public DocWriteRequest<?> createIndexRequest(ConfiguredDarkCanaryIndexDocument configuredDarkCanaryIndexDocument) {
        Integer ttl = Optional.ofNullable(configuredDarkCanaryIndexDocument.darkCanaryConfiguration().getTtl())
                .orElse(maxTllDays);

        DarkCanaryIndexDocument darkCanaryIndexDocument = configuredDarkCanaryIndexDocument.darkCanaryIndexDocument();

        String writeAliasFor = darkCanaryIndexManager.getWriteAliasFor(darkCanaryIndexDocument.getDomain(), darkCanaryIndexDocument.getEnv(), ttl);

        if (darkCanaryIndexDocument.isAggregated()) {
            log.trace("Updating existing document {}", darkCanaryIndexDocument);
            return new UpdateRequest(writeAliasFor, darkCanaryIndexDocument.get_id())
                    .doc(objectMapper.writeValueAsString(darkCanaryIndexDocument), XContentType.JSON);
        } else {
            log.trace("Indexing new document {}", darkCanaryIndexDocument);
            return new IndexRequest(writeAliasFor)
                    .source(objectMapper.writeValueAsString(darkCanaryIndexDocument), XContentType.JSON)
                    .opType(DocWriteRequest.OpType.CREATE);
        }
    }

    @SneakyThrows
    public DocWriteRequest<?> createIndexRequestForDifferences(ConfiguredDarkCanaryIndexDocumentByDifference configuredDarkCanaryIndexDocumentByDifference) {
        Integer ttl = Optional.ofNullable(configuredDarkCanaryIndexDocumentByDifference.darkCanaryConfiguration().getTtl())
                .orElse(maxTllDays);

        DarkCanaryIndexDocumentByDifference darkCanaryIndexDocumentByDifference = configuredDarkCanaryIndexDocumentByDifference.darkCanaryIndexDocumentByDifference();

        String writeAliasFor = darkCanaryIndexManager.getDiffWriteAliasFor(darkCanaryIndexDocumentByDifference.getDomain(),
                darkCanaryIndexDocumentByDifference.getEnv(), ttl);

        return new IndexRequest(writeAliasFor)
                .source(objectMapper.writeValueAsString(darkCanaryIndexDocumentByDifference), XContentType.JSON)
                .opType(DocWriteRequest.OpType.CREATE);
    }
}
