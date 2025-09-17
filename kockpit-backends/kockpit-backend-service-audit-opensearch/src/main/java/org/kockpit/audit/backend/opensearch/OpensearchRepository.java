package org.kockpit.audit.backend.opensearch;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.backend.DomainApiDelegate;
import org.kockpit.audit.backend.Page;
import org.kockpit.audit.backend.opensearch.deprecation.AuditReportHelper;
import org.kockpit.audit.backend.opensearch.dto.AuditReport;
import org.kockpit.audit.backend.opensearch.dto.AuditReportPage;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.FieldSortBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.kockpit.audit.backend.opensearch.deprecation.SearchQueryHelper.constructQuery;

@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class OpensearchRepository implements DomainApiDelegate {

    private final RestHighLevelClient client;

    @SneakyThrows
    @Override
    public ResponseEntity<Page> searchAudits(String query, String domain, String env, Integer start, Integer size) {

        QueryBuilder queryBuilder = constructQuery(queries, auditViewDto.getAppIds());

        SearchSourceBuilder searchSourceBuilder =
                new SearchSourceBuilder()
                        .query(queryBuilder)
                        .sort(new FieldSortBuilder(AuditReportHelper.FIELD_START).order(SortOrder.DESC))
                        .trackTotalHits(true)
                        .from(start)
                        .size(size);

        SearchRequest searchRequest = new SearchRequest()
                .source(searchSourceBuilder)
                .indices(AuditReportHelper.getAuditAliasName(domain, indexName, env));

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        List<AuditReport> items = Arrays.stream(searchResponse.getHits().getHits())
                .map(AuditReportHelper::convertForViewList).toList();

        AuditReportPage auditReportPage = AuditReportPage.builder()
                .items(items)
                .totalSize(Optional.ofNullable(searchResponse.getHits())
                        .map(SearchHits::getTotalHits)
                        .map(totalHits -> totalHits.value)
                        .orElse(0L)
                )
                .from(start)
                .size(size)
                .build();

        return ResponseEntity.ok(Page.builder()
                        .items(Arrays.asList(auditReportPage.getItems().toArray()))
                        .size(auditReportPage.getSize().longValue())
                        .totalCount(auditReportPage.getTotalSize())
                .build());
    }
}
