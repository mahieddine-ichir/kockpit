package com.example.kockpitbackendserviceopensearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.backend.DomainApiDelegate;
import org.kockpit.audit.backend.Page;
import org.kockpit.audit.backend.model.SearchAuditReport;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class OpenSearchRepository implements DomainApiDelegate {

    private final OpenSearchClient client;
    private final String indexName;

    @Override
    public ResponseEntity<Page> searchAudits(String query, String domain, String env, Integer start, Integer size) {
        try {
            SearchResponse<SearchAuditReport> response = doSearch(query, domain, env, start, size);

            List<SearchAuditReport> list = response.hits().hits().stream()
                    .map(Hit::source)
                    .toList();

            return ResponseEntity.ok(Page.builder()
                    .items(new ArrayList<>(list))
                    .size((long) list.size())
                    .totalCount(response.hits().total().value())
                    .build());
        } catch (Exception e) {
            log.error("opensearch search failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<Object> auditsByIdForDomainAndEnv(String id, String domain, String env) {
        try {
            SearchResponse<SearchAuditReport> response = client.search(s -> s
                    .index(indexName)
                    .query(q -> q
                            .bool(b -> b
                                    .must(m -> m.term(t -> t.field("_id").value(v -> v.stringValue(id))))
                                    .must(m -> m.term(t -> t.field("domain").value(v -> v.stringValue(domain))))
                                    .must(m -> m.term(t -> t.field("env").value(v -> v.stringValue(env))))
                            )
                    ), SearchAuditReport.class);

            if (response.hits().hits().isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(response.hits().hits().get(0).source());
        } catch (Exception e) {
            log.error("error searching for document with id: {}", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<Page> listAudits(String domain, String env, Integer start, Integer size) {
        return searchAudits("*", domain, env, start, size);
    }

    private SearchResponse<SearchAuditReport> doSearch(String queryString, String domain, String env, Integer start, Integer size) {
        try {
            Query filterQuery = Query.of(q -> q
                    .bool(b -> b
                            .must(m -> m
                                    .term(t -> t
                                            .field("domain")
                                            .value(v -> v.stringValue(domain))))
                            .must(m -> m
                                    .term(t -> t
                                            .field("env")
                                            .value(v -> v.stringValue(env))))
                    ));

            Query mainQuery;
            if ("*".equals(queryString)) {
                mainQuery = Query.of(q -> q.matchAll(m -> m));
            } else {
                mainQuery = Query.of(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .queryString(qs -> qs
                                                .query(queryString)))
                                .filter(filterQuery)
                        ));
            }

            SearchRequest request = SearchRequest.of(s -> s
                    .index(indexName)
                    .query(mainQuery)
                    .from(start)
                    .size(size)
                    .sort(so -> so.field(f -> f.field("start").order(SortOrder.Desc)))
                    .trackTotalHits(t -> t.enabled(true)));

            return client.search(request, SearchAuditReport.class);

        } catch (Exception e) {
            log.error("error executing search ", e);
            throw new RuntimeException("eearch failed", e);
        }
    }
}