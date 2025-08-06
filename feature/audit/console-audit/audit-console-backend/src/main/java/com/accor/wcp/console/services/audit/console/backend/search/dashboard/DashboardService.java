package com.accor.wcp.console.services.audit.console.backend.search.dashboard;

import com.accor.wcp.console.services.audit.console.backend.search.AuditReportHelper;
import com.accor.wcp.console.services.audit.console.backend.search.ElasticSearchClientWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
class DashboardService {

  private final ElasticSearchClientWrapper client;

  private final DashboardDtoMapper dashboardDtoMapper;

  public DashboardPage byKeyValue(String key, String value, Integer from, Integer size) {

    BoolQueryBuilder queryBuilder = new BoolQueryBuilder();

    BoolQueryBuilder boolQueryBuilderForNested = QueryBuilders.boolQuery();
    boolQueryBuilderForNested.must(QueryBuilders.matchQuery("indexedExtensions.indexedKeyValues.key", key));
    boolQueryBuilderForNested.must(QueryBuilders.matchQuery("indexedExtensions.indexedKeyValues.value", value));
    queryBuilder.must(QueryBuilders.nestedQuery("indexedExtensions.indexedKeyValues", boolQueryBuilderForNested, ScoreMode.None));

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
            .query(queryBuilder)
            .trackTotalHits(true)
            .sort(AuditReportHelper.FIELD_START, SortOrder.ASC)
            .from(from)
            .size(size);

    SearchRequest searchRequest = new SearchRequest()
            .source(searchSourceBuilder)
            .indices("*-auditdata-*");

    SearchResponse search = client.search(searchRequest);

    List<DashboardSearchResultDto> list = dashboardDtoMapper.ofList(Arrays.asList(search.getHits().getHits()));

    return DashboardPage.builder()
            .items(list)
            .size(size)
            .from(from)
            .totalSize(search.getHits().getTotalHits().value)
            .build();
  }
}
