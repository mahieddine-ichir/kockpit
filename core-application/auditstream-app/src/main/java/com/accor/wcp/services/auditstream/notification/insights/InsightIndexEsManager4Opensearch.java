package com.accor.wcp.services.auditstream.notification.insights;

import com.accor.wcp.services.auditstream.notification.insights.model.InsightIndexRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.DocWriteRequest;
import org.opensearch.action.bulk.BulkItemResponse;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
class InsightIndexEsManager4Opensearch {

  private ObjectMapper objectMapper;

  private final InsightsIndexManager indexManager;

  private final RestHighLevelClient restHighLevelClient;

  @PostConstruct
  void init() {
    objectMapper = new ObjectMapper()
            .registerModules(new Jdk8Module())
            .registerModule(new JavaTimeModule());
  }

  @SneakyThrows
  void bulkInsightIndexRequests(List<InsightIndexRequest> insightIndexRequests) {
    if (CollectionUtils.isEmpty(insightIndexRequests)) {
      log.trace("no data to index");
      return;
    }

    BulkRequest bulkRequest = insightIndexRequests.stream()
            .map(this::toIndexRequest)
            .collect(BulkRequest::new, BulkRequest::add, (req1, req2) ->
                    req1.add(req2.requests()));

    BulkResponse bulkItemResponses = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    if (bulkItemResponses.hasFailures()) {
      Stream.of(bulkItemResponses.getItems())
              .filter(BulkItemResponse::isFailed)
              .forEach(bulkItemResponse -> log.error("Bulk item in error {}", bulkItemResponse.getFailureMessage()));
    }
  }

  private IndexRequest toIndexRequest(InsightIndexRequest insightIndexRequest) {
    String writeAliasFor = indexManager.getWriteAliasFor(insightIndexRequest.getDomain(), insightIndexRequest.getEnv(), insightIndexRequest.getTtl());

    return new IndexRequest(writeAliasFor)
            .id(insightIndexRequest.getDocId())
            .source(asJson(insightIndexRequest), XContentType.JSON)
            .opType(DocWriteRequest.OpType.CREATE);
  }

  @SneakyThrows
  String asJson(InsightIndexRequest insightIndexRequest) {
    return objectMapper.writeValueAsString(insightIndexRequest.getData());
  }
}
