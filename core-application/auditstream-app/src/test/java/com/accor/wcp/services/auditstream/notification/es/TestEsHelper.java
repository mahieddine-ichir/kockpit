package com.accor.wcp.services.auditstream.notification.es;

import com.accor.wcp.services.auditstream.notification.es.manager.AuditIndexManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.FieldSortBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class TestEsHelper {

  @Autowired AuditIndexManager auditIndexEsManager;
  @Autowired RestHighLevelClient restHighLevelClient;
  @Autowired ObjectMapper objectMapper;

  public void waitForIndexInitialized() throws InterruptedException {
    log.info("Indice inititialized ? -> {} ", !auditIndexEsManager.getAliasWriteAlreadyInit().isEmpty());
    Awaitility.await("Wait for index elastic initialized after first kinesis record received")
        .pollInterval(1, TimeUnit.SECONDS)
        .atMost(360, TimeUnit.SECONDS)
        .until(() -> !auditIndexEsManager.getAliasWriteAlreadyInit().isEmpty());

    // Wait for doc to be inserted
    Thread.sleep(2000);
  }

  public Set<String> getAliasWriteAlreadyInit() {
    return auditIndexEsManager.getAliasWriteAlreadyInit();
  }

  public SearchResponse getAllOpenSearchResults(String domain, String env) throws IOException {
    String alias = auditIndexEsManager.getAliasRead(domain, env);
    BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
    boolBuilder.must(QueryBuilders.matchAllQuery());
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    sourceBuilder.query(boolBuilder).sort(new FieldSortBuilder("start").order(SortOrder.DESC));
    SearchRequest searchRequest = new SearchRequest(alias);
    searchRequest.source(sourceBuilder);
    return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
  }

  public AuditReportDocument getLastOpenSearchDocument(String domain, String env) throws IOException {
    SearchResponse response = getAllOpenSearchResults(domain, env);
    log.info("Nb docs in indices : {}", response.getHits().getHits().length);

    AuditReportDocument auditReportDocument =
        objectMapper.readValue(
            response.getHits().getHits()[0].getSourceAsString(), AuditReportDocument.class);
    return auditReportDocument;
  }

  public List<SearchHit> getAllSearchHits(String domain, String env) throws IOException {
    SearchResponse response = getAllOpenSearchResults(domain, env);

    return List.of(response.getHits().getHits());
  }
}
