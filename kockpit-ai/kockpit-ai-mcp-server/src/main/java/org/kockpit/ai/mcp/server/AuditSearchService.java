package org.kockpit.ai.mcp.server;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.join.ScoreMode;
import org.kockpit.ai.mcp.server.dto.AuditDataPage;
import org.kockpit.ai.mcp.server.dto.AuditReport;
import org.kockpit.ai.mcp.server.dto.AuditReportPage;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.NestedQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.FieldSortBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.opensearch.index.query.QueryBuilders.matchQuery;
import static org.opensearch.index.query.QueryBuilders.termsQuery;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuditSearchService {

    private final ElasticSearchClientWrapper client;

    @Value("${opensearch.env:*}")
    private String defaultEnv;

    @Value("${opensearch.search.index_version:wcp}")
    private String indexVersion;

    @Tool(name = "search-audits-by-traceId", description = "Search Audits by traceId for a given domain")
    @SneakyThrows
    public AuditReportPage searchByTraceId(
            @ToolParam(description = "audits/requests Trace ID (traceId)") String traceId,
            @ToolParam(required = false, description = "audits domain, default to all domains (*)") String domain,
            @ToolParam(required = false, description = "start/from for paging, default to 0") Integer from,
            @ToolParam(required = false, description = "max number of audits per search, default to 25, max to 50") Integer size
    ) {
        if (traceId == null || traceId.isBlank()) {
            throw new IllegalArgumentException("traceId is required and cannot be empty");
        }

        from = isNull(from) ? 0 : max(from, 0);
        size = isNull(size) ? 25 : min(size, 50);

        String index = "%s-auditdata-%s-read".formatted(
                nonNull(domain) ? domain : "*",
                defaultEnv
        );

        IndexKeyValuesForNested indexKeyValuesForNested = resolveIndexKeyValuesPath(indexVersion);

        BoolQueryBuilder traceIdQuery = QueryBuilders.boolQuery();

        traceIdQuery
                .must(termsQuery(indexKeyValuesForNested.keyPath(), "traceId", "X-B3-TraceId"))
                .must(matchQuery(indexKeyValuesForNested.valuePath(), traceId));

        NestedQueryBuilder queryBuilder = QueryBuilders.nestedQuery(
                indexKeyValuesForNested.rootPath(), traceIdQuery, ScoreMode.None
        );

        System.out.printf("""
                Generated query: %n
                        %s
                %n""", queryBuilder);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(queryBuilder)
                .sort(new FieldSortBuilder("start").order(SortOrder.DESC))
                .trackTotalHits(true)
                .from(from)
                .size(size);

        return runRequest(from, size, searchSourceBuilder, index);
    }

    @Tool(name = "get-kengine-flows", description = "Get kengine.flows audits from the original (decompressed) audit report for a given traceId")
    @SneakyThrows
    public AuditDataPage getKengineFlows(
            @ToolParam(description = "audits/requests Trace ID (traceId)") String traceId,
            @ToolParam(required = false, description = "audits domain, default to all domains (*)") String domain,
            @ToolParam(required = false, description = "start/from for paging, default to 0") Integer from,
            @ToolParam(required = false, description = "max number of audits per search, default to 25, max to 50") Integer size
    ) {
        return fetchAuditDataPage(traceId, domain, from, size, AuditReportHelper::extractKengineFlowsAudits);
    }

    @Tool(name = "get-mock-httpexchanges", description = "Get builtin.httpexchanges where domain or request path contains 'mock', returning only request and response for each matching exchange")
    @SneakyThrows
    public AuditDataPage getMockHttpExchanges(
            @ToolParam(description = "audits/requests Trace ID (traceId)") String traceId,
            @ToolParam(required = false, description = "audits domain, default to all domains (*)") String domain,
            @ToolParam(required = false, description = "start/from for paging, default to 0") Integer from,
            @ToolParam(required = false, description = "max number of audits per search, default to 25, max to 50") Integer size
    ) {
        return fetchAuditDataPage(traceId, domain, from, size, AuditReportHelper::extractMockHttpExchanges);
    }

    @Tool(name = "get-builtin-httpexchanges", description = "Get builtin.httpexchanges audits from the original (decompressed) audit report for a given traceId")
    @SneakyThrows
    public AuditDataPage getBuiltinHttpExchanges(
            @ToolParam(description = "audits/requests Trace ID (traceId)") String traceId,
            @ToolParam(required = false, description = "audits domain, default to all domains (*)") String domain,
            @ToolParam(required = false, description = "start/from for paging, default to 0") Integer from,
            @ToolParam(required = false, description = "max number of audits per search, default to 25, max to 50") Integer size
    ) {
        return fetchAuditDataPage(traceId, domain, from, size, AuditReportHelper::extractBuiltinHttpExchangesAudits);
    }

    @SneakyThrows
    private AuditDataPage fetchAuditDataPage(String traceId, String domain, Integer from, Integer size,
                                             Function<String, List<Map<String, Object>>> extractor) {
        from = isNull(from) ? 0 : max(from, 0);
        size = isNull(size) ? 25 : min(size, 50);

        String index = "%s-auditdata-%s-read".formatted(
                nonNull(domain) ? domain : "*",
                defaultEnv
        );

        IndexKeyValuesForNested indexKeyValuesForNested = resolveIndexKeyValuesPath(indexVersion);

        BoolQueryBuilder traceIdQuery = QueryBuilders.boolQuery()
                .must(termsQuery(indexKeyValuesForNested.keyPath(), "traceId", "X-B3-TraceId"))
                .must(matchQuery(indexKeyValuesForNested.valuePath(), traceId));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.nestedQuery(indexKeyValuesForNested.rootPath(), traceIdQuery, ScoreMode.None))
                .sort(new FieldSortBuilder("start").order(SortOrder.DESC))
                .trackTotalHits(true)
                .from(from)
                .size(size);

        SearchRequest searchRequest = new SearchRequest().source(searchSourceBuilder).indices(index);
        SearchResponse searchResponse = client.search(searchRequest);

        List<Map<String, Object>> items = Arrays.stream(searchResponse.getHits().getHits())
                .map(hit -> AuditReportHelper.convertFromString(hit.getSourceAsString()))
                .filter(doc -> nonNull(doc.getCompressedOriginalJsonValue()))
                .map(doc -> AuditReportHelper.decompressOriginalJson(doc.getCompressedOriginalJsonValue()))
                .flatMap(json -> extractor.apply(json).stream())
                .toList();

        return AuditDataPage.builder()
                .items(items)
                .totalSize(Optional.ofNullable(searchResponse.getHits())
                        .map(SearchHits::getTotalHits)
                        .map(TotalHits::value)
                        .orElse(0L))
                .from(from)
                .size(size)
                .build();
    }

    private AuditReportPage runRequest(Integer from, Integer size, SearchSourceBuilder searchSourceBuilder, String index) {
        try {
            SearchRequest searchRequest = new SearchRequest()
                    .source(searchSourceBuilder)
                    .indices(index);

            SearchResponse searchResponse = client.search(searchRequest);
            List<AuditReport> items = Arrays.stream(searchResponse.getHits().getHits())
                    .map(AuditReportHelper::convertForViewList).toList();

            return AuditReportPage.builder()
                    .items(items)
                    .totalSize(Optional.ofNullable(searchResponse.getHits())
                            .map(SearchHits::getTotalHits)
                            .map(TotalHits::value)
                            .orElse(0L)
                    )
                    .from(from)
                    .size(size)
                    .build();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return AuditReportPage.builder().build();
        }
    }

    private IndexKeyValuesForNested resolveIndexKeyValuesPath(String indexVersion) {

        if (indexVersion.equals("wcp")) {
            return new IndexKeyValuesForNested(
                    "indexedExtensions.indexedKeyValues",
                    "indexedExtensions.indexedKeyValues.key",
                    "indexedExtensions.indexedKeyValues.value"
            );
        } else {
            return new IndexKeyValuesForNested(
                    "indexedKeyValues",
                    "indexedKeyValues.key",
                    "indexedKeyValues.value"
            );
        }
    }
}
