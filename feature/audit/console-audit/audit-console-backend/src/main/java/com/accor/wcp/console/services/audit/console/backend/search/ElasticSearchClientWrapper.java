package com.accor.wcp.console.services.audit.console.backend.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.search.SearchHit;
import org.springframework.stereotype.Component;

/** Wrapper class to be able to mock {@link #search(SearchRequest, RequestOptions)}. */
@Component
@Slf4j
@RequiredArgsConstructor
public class ElasticSearchClientWrapper {

  private final RestHighLevelClient client;

  /**
   * Delegate method.
   * @deprecated do not support pagination
   * @see RestHighLevelClient#search(SearchRequest, RequestOptions)
   */
  public List<SearchHit> search(SearchRequest searchRequest, RequestOptions options)
      throws IOException {
    List<SearchHit> mergedSearchHits;

    // Search in new storage
    try {
      SearchResponse searchResponse = client.search(searchRequest, options);
      mergedSearchHits = new ArrayList<>(Arrays.asList(searchResponse.getHits().getHits()));
    } catch (Exception e) {
      mergedSearchHits = new ArrayList<>();
    }

    return mergedSearchHits;
  }

  @SneakyThrows
  public SearchResponse search(SearchRequest searchRequest) {
    return client.search(searchRequest, RequestOptions.DEFAULT);
  }

}
