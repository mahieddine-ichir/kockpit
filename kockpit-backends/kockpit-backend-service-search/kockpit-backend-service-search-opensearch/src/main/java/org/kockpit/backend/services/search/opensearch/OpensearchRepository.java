package org.kockpit.backend.services.search.opensearch;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
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
import org.opensearch.index.query.NestedQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.FieldSortBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
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
        log.trace("load audit for domain {}, env {}, id {}", domain, env, id);
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(matchQuery("id", id));
        boolQueryBuilder.must(matchQuery("env", env));
        boolQueryBuilder.must(matchQuery("domain", domain));

        SearchSourceBuilder searchSourceBuilder =
                new SearchSourceBuilder()
                        .query(boolQueryBuilder)
                        .sort(new FieldSortBuilder("start").order(SortOrder.DESC))
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
            buildQuery(searchTerms, rootBoolQueryBuilder);
        }

        Optional.ofNullable(query)
                .map(String::toLowerCase)
                .map(q -> Arrays.stream(q.split(" ")))
                .ifPresent(texts -> texts.forEach(text -> {
                    if (text.contains("*")) {
                        rootBoolQueryBuilder
                                .should(wildcardQuery("audits.events.httpAuditedRequest.body", text))
                                .should(wildcardQuery("audits.events.httpAuditedResponse.body", text))
                                .should(wildcardQuery("audits.events.httpAuditedResponse.payload", text));
                    } else {
                        rootBoolQueryBuilder.should(multiMatchQuery(text, "*"));
                    }
                }));

        return runQuery(rootBoolQueryBuilder, domain, env, start, size);
    }

    private void buildQuery(List<SearchTerm> searchTerms, BoolQueryBuilder rootBoolQueryBuilder) {
        // group searchTerms by name/path
        searchTerms.stream().collect(Collectors.groupingBy(searchTerm -> isNull(searchTerm.getPath()) ? searchTerm.getName() : searchTerm.getPath()))
                .forEach((path, terms) -> {
                    // get values
                    List<Object> values = new ArrayList<>();
                    terms.stream().map(SearchTerm::getValue).forEach(value -> {
                        if (value instanceof List<?> list) {
                            values.addAll(list);
                        } else {
                            values.add(value);
                        }
                    });
                    this.buildQueryWithSamePath(path, values, rootBoolQueryBuilder);
                });
    }

    private void buildQueryWithSamePath(String path, List<Object> values, BoolQueryBuilder rootBoolQueryBuilder) {
        log.trace("search on path {}, for values {}", path, values);
        if (path.contains(",")) {
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            Stream.of(path.split(",")).map(String::trim)
                    .forEach(p -> {
                        if (p.startsWith("indexedKeyValues")) {
                            String key = p.substring("indexedKeyValues".length() + 1);
                            boolQueryBuilder.should(buildQueryForIndexedKeyValues(key, values));
                        } else {
                            boolQueryBuilder.should(buildValuesQuery(p, values));
                        }
                    });
            rootBoolQueryBuilder.must(boolQueryBuilder);
        } else {
            // indexedKeyValues -> nested search
            if (path.startsWith("indexedKeyValues")) {
                log.trace("nested search for indexedKeyValues on path {}", path);
                String key = path.substring("indexedKeyValues".length() + 1);
                NestedQueryBuilder nestedQueryBuilder = buildQueryForIndexedKeyValues(key, values);
                rootBoolQueryBuilder.must(nestedQueryBuilder);

            } else if ("start".equals(path) || "end".equals(path)) {
                log.trace("time-base search on path {}", path);
                // timestamp search
                long value = (long) values.get(0);
                if ("start".equals(path)) {
                    rootBoolQueryBuilder.must(rangeQuery(path).gte(value));
                }
                if ("end".equals(path)) {
                    rootBoolQueryBuilder.must(rangeQuery(path).lt(value));
                }
            } else {
                log.trace("basic search on path {}", path);
                rootBoolQueryBuilder.must(buildValuesQuery(path, values));
            }
        }
    }

    private static boolean isWildcard(Object value) {
        return value instanceof String s && s.contains("*");
    }

    /**
     * Builds the query for a single value on a given field. When the value contains one or more
     * '*', a wildcard query is used so the '*' position drives the semantics natively:
     * {@code abc*} -> startsWith, {@code *abc} -> endsWith, {@code *abc*} -> contains,
     * {@code ab*cd} -> arbitrary pattern. Otherwise a regular (analyzed) match query is used.
     */
    private QueryBuilder buildValueQuery(String field, Object value) {
        if (isWildcard(value)) {
            return wildcardQuery(field, ((String) value).toLowerCase());
        }
        return matchQuery(field, value);
    }

    /**
     * Builds the query for one or more values on a given field. If any value contains a '*',
     * each value is turned into its own (wildcard or match) query combined with a should/OR,
     * otherwise a single terms query is used for the multi-value case.
     */
    private QueryBuilder buildValuesQuery(String field, List<?> values) {
        if (values.size() == 1) {
            return buildValueQuery(field, values.get(0));
        }
        if (values.stream().anyMatch(OpensearchRepository::isWildcard)) {
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            values.forEach(value -> boolQueryBuilder.should(buildValueQuery(field, value)));
            return boolQueryBuilder;
        }
        return termsQuery(field, values);
    }

    private NestedQueryBuilder buildQueryForIndexedKeyValues(String name, List<?> values) {
        log.trace("Searching {} in [{}]", name, values);
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder
                .must(matchQuery("indexedKeyValues.key", name))
                .must(buildValuesQuery("indexedKeyValues.value", values));
        return nestedQuery("indexedKeyValues", boolQueryBuilder, ScoreMode.None);
    }

    private Page runQuery(BoolQueryBuilder rootBoolQueryBuilder, String domain, String env, Integer start, Integer size) throws Exception {
        try {
            SearchSourceBuilder searchSourceBuilder =
                    new SearchSourceBuilder()
                            .query(rootBoolQueryBuilder)
                            .sort(new FieldSortBuilder("start").order(SortOrder.DESC))
                            .trackTotalHits(true)
                            .fetchSource("*", "audits")
                            .from(start)
                            .size(size);

            SearchRequest searchRequest = new SearchRequest()
                    .source(searchSourceBuilder)
                    .indices(getAuditAliasName(domain, index, env));

            SearchHits hits = client.search(searchRequest, RequestOptions.DEFAULT)
                    .getHits();

            return Page.builder()
                    .totalCount(hits.getTotalHits() == null ? 0 : hits.getTotalHits().value())
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
