package com.accor.wcp.services.auditstream.notification.es.manager.opensearch;

import com.accor.wcp.services.auditstream.notification.es.manager.AuditIndexManager;
import com.accor.wcp.services.auditstream.notification.es.manager.AuditIndexRequest;
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
import org.opensearch.core.rest.RestStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.nonNull;

@Profile("opensearch")
@Component
@RequiredArgsConstructor
@Slf4j
class AuditIndexEsManager4Opensearch implements AuditIndexManager {

  private final RestHighLevelClient restHighLevelClient;

  private final IndexCreationClient indexCreationClient;

  @Value("${opensearch.index_mapping_file}")
  private String indexMappingFile;

  @Value("${opensearch.index_suffix}")
  private String indexSuffix;

  public String getWriteAliasFor(String domain, String env, int ttl) {
    return indexCreationClient.getWriteAliasFor(domain, indexSuffix, env, ttl, indexMappingFile);
  }

  IndexRequest createIndexRequest(
      String docId, String domain, String env, int ttl, String auditReportDocument) {
    String writeAlias = indexCreationClient.getWriteAliasFor(domain, indexSuffix, env, ttl, indexMappingFile);

    return createIndexRequest(docId, auditReportDocument, writeAlias);
  }

  IndexRequest createIndexRequest(String requestId, String auditReportDocument, String alias) {
    return new IndexRequest(alias)
        .id(requestId)
        .source(auditReportDocument, XContentType.JSON)
        .opType(DocWriteRequest.OpType.CREATE);
  }

  @Override
  public void bulkAuditIndexRequests(List<AuditIndexRequest> auditIndexRequests)
      throws IOException {
    // TODO - first validate index before?
    List<IndexRequest> indexRequests =
          auditIndexRequests.stream().map(this::createIndexRequest).toList();
      // TODO review all
      BulkRequest request = new BulkRequest();
      indexRequests.forEach(request::add);

      BulkResponse bulkItemResponses;
      try {
        bulkItemResponses = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
      } catch (IOException e) {
        throw new IOException("Error while bulk index: " + indexRequests, e);
      }

    // Manage failures, skip conflict status = document already exists
    if (!bulkItemResponses.hasFailures()) {
      return;
    }
    List<BulkItemResponse> bulkItemResponseErrors = Arrays.stream(bulkItemResponses.getItems())
        .filter(bulkItemResponse -> nonNull(bulkItemResponse.getFailure()))
        .map(
            bulkItemResponse -> {
              if (RestStatus.CONFLICT.equals(bulkItemResponse.getFailure().getStatus())) {
                return null;
              }
              return bulkItemResponse;
            })
        .filter(Objects::nonNull)
        .toList();

    if (!bulkItemResponseErrors.isEmpty()) {
      log.error("Audit request(s) in error: {}", bulkItemResponseErrors);
    }
  }

  @SneakyThrows
  private IndexRequest createIndexRequest(AuditIndexRequest auditIndexRequest) {
      return createIndexRequest(
          auditIndexRequest.getDocId(),
          auditIndexRequest.getDomain(),
          auditIndexRequest.getEnv(),
          auditIndexRequest.getTtl(),
          auditIndexRequest.getAuditReportJson());
  }

  @Override
  public String getAliasRead(String domain, String env) {
    return OpensearchHelper.getAliasRead(domain, indexSuffix, env);
  }

  @Deprecated
  public Set<String> getAliasWriteAlreadyInit() {
    return indexCreationClient.getAliasWriteAlreadyInit();
  }
}
