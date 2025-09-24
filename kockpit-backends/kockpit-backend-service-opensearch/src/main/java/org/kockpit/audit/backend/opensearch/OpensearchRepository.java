package org.kockpit.audit.backend.opensearch;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.backend.DomainApiDelegate;
import org.kockpit.audit.backend.Page;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.FieldSortBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.kockpit.audit.backend.opensearch.AuditReportHelper.getAuditAliasName;
import static org.kockpit.audit.backend.opensearch.SearchQueryHelper.constructQuery;

@RequestMapping
@RequiredArgsConstructor
@Slf4j
// fixme implement complex search inputs (with operands ... etc)
public class OpensearchRepository implements DomainApiDelegate {

    private final RestHighLevelClient client;

    private final String index;

    @Override
    public ResponseEntity<Page> listAudits(String domain, String env, Integer start, Integer size) {
        return this.searchAudits(  domain, env,null,null,  start, size);
    }

    @SneakyThrows
    @Override
    public ResponseEntity<Page> searchAudits(String domain, String env, String query, String status, Integer start, Integer size) {
        return this.searchAuditsWithFilters(query, status, domain, env, start, size);
    }

    @SneakyThrows
    public ResponseEntity<Page> searchAuditsWithFilters(
            @Nullable String query,
            @Nullable String status,
            String domain,
            String env,
            Integer start,
            Integer size) {

        List<String> texts = Optional.ofNullable(query)
                .map(q -> Arrays.stream(q.split(" ")).toList())
                .orElse(new ArrayList<>());

        QueryBuilder queryBuilder = constructQuery(texts, status);
        SearchSourceBuilder searchSourceBuilder =
                new SearchSourceBuilder()
                        .query(queryBuilder)
                        .sort(new FieldSortBuilder("start").order(SortOrder.DESC))
                        .trackTotalHits(true)
                        .from(start)
                        .size(size);

        SearchRequest searchRequest = new SearchRequest()
                .source(searchSourceBuilder)
                .indices(getAuditAliasName(domain, index, env));

        SearchHits hits = client.search(searchRequest, RequestOptions.DEFAULT).getHits();

        return ResponseEntity.ok(Page.builder()
                .totalCount(hits.getTotalHits() == null ? 0 : hits.getTotalHits().value)
                .size((long) hits.getHits().length)
                .items(fromHits(hits))
                .build());
    }

    @SneakyThrows
    @Override
    public ResponseEntity<Object> auditsByIdForDomainAndEnv(String id, String domain, String env) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.matchQuery("id", id));
        boolQueryBuilder.must(QueryBuilders.matchQuery("env", env));
        boolQueryBuilder.must(QueryBuilders.matchQuery("domain", domain));

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
        return ResponseEntity.ok(searchResponse.getHits().getAt(0).getSourceAsMap());
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
