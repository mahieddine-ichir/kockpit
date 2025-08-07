package org.kockpit.audit.backend.azuresearch;

import com.azure.core.util.Context;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.util.SearchPagedIterable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.backend.DomainApiDelegate;
import org.kockpit.audit.backend.Page;
import org.kockpit.audit.backend.model.SearchAuditReport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class AzureSearchRepository implements DomainApiDelegate {

    private final SearchClient client;

    @Value("${kockpit.audit.azure.search.max_size:25}")
    private Integer maxSize;

    @Override
    public ResponseEntity<Page> searchAudits(String query, String domain, String env, Integer start, Integer size) {
        log.debug("Search audits {} for query {} and domain {} and env {} and start {} and size {}", query, domain, env, query, start, size);
        SearchPagedIterable search = doSearch(query, domain, env, start, size);
        List<SearchAuditReport> list = search
                .stream()
                .map(searchResult -> searchResult.getDocument(SearchAuditReport.class))
                .toList();

        log.debug("Found {} audits for query {} and domain {} and env {}", list.size(), query, domain, env);
        return ResponseEntity.ok(Page.builder()
                .items(new ArrayList<>(list))
                .size((long) list.size())
                .totalCount(search.getTotalCount())
                .build());
    }

    @Override
    public ResponseEntity<Object> auditsByIdForDomainAndEnv(String id, String domain, String env) {
        Object document = client.getDocument(id, SearchAuditReport.class);
        return ResponseEntity.ok(document);
    }

    @Override
    public ResponseEntity<Page> listAudits(String domain, String env, Integer start, Integer size) {
        log.debug("load for domain {}, env {}, page {} {}", domain, env, start, size);
        SearchPagedIterable search = doSearch("*", domain, env, start, size);
        List<SearchAuditReport> list = search
                .stream()
                .map(searchResult -> searchResult.getDocument(SearchAuditReport.class))
                .toList();
        return ResponseEntity.ok(Page.builder()
                        .items(new ArrayList<>(list))
                        .size(Math.min(size, search.getTotalCount()))
                        .totalCount(search.getTotalCount())
                .build());
    }

    private SearchPagedIterable doSearch(String query, String domain, String env, Integer start, Integer size) {
        SearchOptions searchOptions = new SearchOptions()
                .setOrderBy("start desc")
                .setFilter("domain eq '" + domain + "' and env eq '" + env + "'")
                .setSkip(start)
                .setIncludeTotalCount(true)
                .setTop(Math.min(size, maxSize));

        return client.search(query, searchOptions, Context.NONE);
    }
}
