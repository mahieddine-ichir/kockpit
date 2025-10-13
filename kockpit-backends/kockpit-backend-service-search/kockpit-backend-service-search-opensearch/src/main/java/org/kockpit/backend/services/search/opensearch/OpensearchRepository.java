package org.kockpit.backend.services.search.opensearch;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.backend.services.search.Page;
import org.kockpit.backend.services.search.SearchService;
import org.kockpit.backend.services.search.SearchTerm;
import org.opensearch.OpenSearchStatusException;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.FieldSortBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.kockpit.backend.services.search.opensearch.AuditReportHelper.getAuditAliasName;
import static org.opensearch.index.query.QueryBuilders.*;

@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class OpensearchRepository implements SearchService {

    private final RestHighLevelClient client;

    private final String index;

    @SneakyThrows
    @Override
    public Object getAudit(String domain, String env, String id) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(matchQuery("id.keyword", id)); // fixme mappings
        boolQueryBuilder.must(matchQuery("env", env));
        boolQueryBuilder.must(matchQuery("domain", domain));

        SearchSourceBuilder searchSourceBuilder =
                new SearchSourceBuilder()
                        .query(boolQueryBuilder)
                        .sort(new FieldSortBuilder("start").order(SortOrder.DESC))
                        .trackTotalHits(true)
                        .size(1);

        SearchRequest searchRequest = new SearchRequest()
                .source(searchSourceBuilder)
                .indices(getAuditAliasName(domain, index, env));

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        return searchResponse.getHits().getHits()[0].getSourceAsMap();
    }

    @SneakyThrows
    @Override
    public Page listAudits(String domain, String env, Integer start, Integer size) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(matchQuery("env", env));
        boolQueryBuilder.must(matchQuery("domain", domain));

        return runQuery(boolQueryBuilder, domain, env, start, size);
    }

    @Override
    public Page searchByQuery(String domain, String env, String query, Integer start, Integer size) {
        return this.searchAudits(domain, env, query, null, start, size);
    }

    @SneakyThrows
    @Override
    public Page searchAudits(String domain, String env, String query, List<SearchTerm> searchTerms, Integer start, Integer size) {
        BoolQueryBuilder rootBoolQueryBuilder = new BoolQueryBuilder();
        if (!CollectionUtils.isEmpty(searchTerms)) {
            log.debug("Search terms {}", searchTerms);
            searchTerms.stream()
                    .filter(searchTerm -> nonNull(searchTerm.getValue()))
                    .forEach(searchTerm -> buildQuery(searchTerm, rootBoolQueryBuilder));
        }

        Optional.ofNullable(query)
                .map(String::toLowerCase)
                .map(q -> Arrays.stream(q.split(" ")))
                .ifPresent(texts -> texts.forEach(text -> {
                    if (text.contains("*")) {
                        // fixme through searchTerm?
                        rootBoolQueryBuilder.should(wildcardQuery("audits.events.httpAuditedRequest.body", text));
                        rootBoolQueryBuilder.should(wildcardQuery("audits.events.httpAuditedResponse.body", text));
                    } else {
                        rootBoolQueryBuilder.should(multiMatchQuery(text, "*"));
                    }
                }));

        return runQuery(rootBoolQueryBuilder, domain, env, start, size);
    }

    private void buildQuery(SearchTerm searchTerm, BoolQueryBuilder rootBoolQueryBuilder) {
        String path = isNull(searchTerm.getPath()) ? searchTerm.getName() : searchTerm.getPath();
        if (path.startsWith("indexedKeyValues")) {
            String key = path.substring("indexedKeyValues".length() + 1);
            if (searchTerm.getValue() instanceof List<?> values) {
                log.debug("Searching terms {} of list {}", key, values);
                buildQueryForIndexedKeyValues(key, values, rootBoolQueryBuilder);
            } else {
                buildQueryForIndexedKeyValues(key, List.of(searchTerm.getValue()), rootBoolQueryBuilder);
            }
        } else if ("start".equals(searchTerm.getName()) || "end".equals(searchTerm.getName())) {
            if ("start".equals(searchTerm.getName())) {
                log.info("Searching from {}", new Date((long) searchTerm.getValue()));
                rootBoolQueryBuilder.must(rangeQuery(path).from(searchTerm.getValue()));
            }
            if ("end".equals(searchTerm.getName())) {
                log.info("Searching to {}", new Date((long) searchTerm.getValue()));
                rootBoolQueryBuilder.must(rangeQuery(path).to(searchTerm.getValue()));
            }
        } else {
            rootBoolQueryBuilder.must(matchQuery(path, searchTerm.getValue()));
        }
    }

    private void buildQueryForIndexedKeyValues(String name, List<?> values, BoolQueryBuilder rootBoolQueryBuilder) {
        log.debug("Searching {} in [{}]", name, values);
        rootBoolQueryBuilder
                .must(matchQuery("indexedKeyValues.key.keyword", name))
                .must(termsQuery("indexedKeyValues.value.keyword", values));
    }

    private Page runQuery(BoolQueryBuilder rootBoolQueryBuilder, String domain, String env, Integer start, Integer size) throws Exception {
        try {
            SearchSourceBuilder searchSourceBuilder =
                    new SearchSourceBuilder()
                            .query(rootBoolQueryBuilder)
                            .sort(new FieldSortBuilder("start").order(SortOrder.DESC))
                            .trackTotalHits(true)
                            .from(start)
                            .size(size);

            SearchRequest searchRequest = new SearchRequest()
                    .source(searchSourceBuilder)
                    .indices(getAuditAliasName(domain, index, env));
            SearchHits hits = client.search(searchRequest, RequestOptions.DEFAULT)
                    .getHits();

            return Page.builder()
                    .totalCount(hits.getTotalHits() == null ? 0 : hits.getTotalHits().value)
                    .size((long) hits.getHits().length)
                    .items(fromHits(hits))
                    .build();

        } catch (Exception e) {
            log.error("Failed to search opensearch results", e);
            if (e instanceof OpenSearchStatusException openSearchStatusException) {
                if (openSearchStatusException.status() == RestStatus.NOT_FOUND) {
                    return Page.builder()
                            .items(Collections.emptyList())
                            .size(0L)
                            .totalCount(0L)
                            .build();
                } else {
                    throw e;
                }
            } else {
                throw e;
            }
        }
    }

    private List<Object> fromHits(SearchHits searchHits) {
        return Stream.of(searchHits.getHits())
                .map(this::map)
                .toList();
    }

    private Object map(SearchHit searchHit) {
        return searchHit.getSourceAsMap();
    }
}
