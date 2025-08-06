package com.accor.wcp.console.services.audit.console.backend.alert;

import static com.accor.wcp.console.services.audit.console.backend.alert.dto.ManagedIndexMetaData.FIELD_ACTION_FAILED;
import static com.accor.wcp.console.services.audit.console.backend.alert.dto.ManagedIndexMetaData.FIELD_POLICY_ID;
import static com.accor.wcp.console.services.audit.console.backend.alert.dto.ManagedIndexMetaData.FIELD_TIMESTAMP;
import static com.accor.wcp.console.services.audit.console.backend.alert.dto.ManagedIndexMetaData.ISM_INDEX_PATTERN;

import com.accor.wcp.console.services.audit.console.backend.alert.dto.ManagedIndexMetaData;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.TermQueryBuilder;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.FieldSortBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class IsmRequestService {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final RestHighLevelClient client;

  @PostConstruct
  void init() {
    objectMapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public List<ManagedIndexMetaData> getManagedIndexOnErrorInfo() {
    return findPolicyIdWithError().stream()
        .map(this::findLastPolicyIdHistory)
        // Action is null when ism init an index
        .filter(e -> e.getAction() != null && e.getAction().isFailed())
        .collect(Collectors.toList());
  }

  @SneakyThrows
  private List<String> findPolicyIdWithError() {
    // Create condition to find policy id with error
    TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(FIELD_ACTION_FAILED, true);
    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(termQueryBuilder);

    // Create aggregation on policy id to limit result
    TermsAggregationBuilder termsAggregation =
        AggregationBuilders.terms("AGG_BY_POLICY_ID")
            .field(FIELD_POLICY_ID)
            // Limit agg result to policyId with min 1 error
            .minDocCount(1)
            // max 1000 policyId
            .size(1000);

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(boolQueryBuilder);
    searchSourceBuilder.aggregation(termsAggregation);
    searchSourceBuilder.size(0);

    SearchRequest searchRequest = new SearchRequest();
    searchRequest.indices(ISM_INDEX_PATTERN);
    searchRequest.source(searchSourceBuilder);

    log.debug("Es generated query {}", searchSourceBuilder);
    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

    if (searchResponse.getAggregations() != null) {
      Terms aggregation = searchResponse.getAggregations().get("AGG_BY_POLICY_ID");
      return aggregation.getBuckets().stream()
          .map(Terms.Bucket::getKeyAsString)
          // Policy id is empty when manual retry on not managed index
          .filter(StringUtils::isNotEmpty)
          .collect(Collectors.toList());
    } else {
      return Collections.emptyList();
    }
  }

  @SneakyThrows
  private ManagedIndexMetaData findLastPolicyIdHistory(String policyId) {
    // Create condition on policyId parameter
    TermQueryBuilder termQuery = QueryBuilders.termQuery(FIELD_POLICY_ID, policyId);
    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(termQuery);

    // Sort by managed_index_meta_data.history_timestamp and get the latest entry (size = 1)
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.sort(new FieldSortBuilder(FIELD_TIMESTAMP).order(SortOrder.DESC));
    searchSourceBuilder.size(1);
    searchSourceBuilder.query(boolQueryBuilder);

    SearchRequest searchRequest = new SearchRequest();
    searchRequest.indices(ISM_INDEX_PATTERN);
    searchRequest.source(searchSourceBuilder);

    log.debug("Es generated query {}", searchSourceBuilder);
    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
    return objectMapper.readValue(
        searchResponse.getHits().getHits()[0].getSourceAsString(), ManagedIndexMetaData.class);
  }
}
