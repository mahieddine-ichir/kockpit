package org.kockpit.audit.stream.opensearch;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditorEventService;
import org.kockpit.audit.api.AuditorKeyValueService;
import org.kockpit.audit.api.AuditorService;
import org.kockpit.audit.api.IndexedKeyValue;
import org.kockpit.audit.module.web.WebAuditEvent;
import org.kockpit.audit.module.web.WebAuditReportData;
import org.kockpit.audit.module.web.request.HttpAuditedRequest;
import org.kockpit.audit.module.web.response.HttpAuditedResponse;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.kockpit.audit.stream.api.AuditStreamJson;
import org.kockpit.audit.stream.api.model.AuditReport;
import org.kockpit.sdk.SdkApplicationProperties;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;
import org.springframework.scheduling.annotation.Scheduled;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.isNull;
import static org.kockpit.audit.stream.opensearch.OpensearchHelper.*;

@Slf4j
@RequiredArgsConstructor
public class AuditConsumerForOpensearch implements AuditConsumer {

    // local cache for batch indexing
    private final List<byte[]> auditReports = new ArrayList<>();

    private final OpenSearchClient openSearchClient;

    private final OpensearchV3IndexManager opensearchV3IndexManager;

    private final AuditorService auditorService;

    private final AuditorKeyValueService auditorKeyValueService;

    private final AuditorEventService auditorEvents;

    private final SdkApplicationProperties sdkApplicationProperties;

    private final ObjectMapper objectMapper;

    private final String indexSuffix;

    private final Integer ttlDefaultInDays;

    private final int bulkBatchSize;

    @PostConstruct
    public void start() {
        log.info("OpenSearch Audit consumer started!");
        Runtime.getRuntime().addShutdownHook(new Thread(this::index));
    }

    @Override
    public void accept(List<byte[]> byteBuffers) {
        byteBuffers.stream().map(bytes -> {
            try {
                return AuditStreamJson.read(bytes);
            } catch (Exception e) {
                log.error("Error reading audit data!", e);
                return null;
            }
        }).filter(Objects::nonNull).forEach(auditReports::add);
    }

    @Scheduled(
            fixedDelayString = "${kockpit.audit.stream.opensearch.scheduler_ms:5000}",
            timeUnit = TimeUnit.MILLISECONDS)
    void index() {
        if (auditReports.isEmpty()) {
            return;
        }
        // defensive copy
        List<byte[]> copy = List.copyOf(auditReports.stream().map(byte[]::clone).toList());
        auditReports.clear();

        this.indexAudits(copy);
    }

    private void indexAudits(List<byte[]> auditReports) {
        auditReports.stream()
                .map(AuditStreamJson::readAuditReport)
                .peek(auditReport -> {
                    if (isNull(auditReport.getTtl())) {
                        auditReport.setTtl(ttlDefaultInDays);
                    }
                })
                .collect(Collectors.groupingBy(IndexMetadata::of))
                .forEach((indexMetadata, auditIndexRequestsGrouped) -> {

                    boolean auditStarter = startAudit(auditIndexRequestsGrouped);

                    log.debug("Start indexing {} reports, for Index {}", auditIndexRequestsGrouped.size(), indexMetadata);
                    long now = System.currentTimeMillis();

                    String indexPrefix = getIndexPrefix(indexMetadata.getDomain(), indexSuffix, indexMetadata.getEnv(), indexMetadata.getTtl());
                    String aliasWrite = getAliasWrite(indexMetadata.getDomain(), indexSuffix, indexMetadata.getEnv(), indexMetadata.getTtl());
                    String aliasRead = getAliasRead(indexMetadata.getDomain(), indexSuffix, indexMetadata.getEnv());

                    String indexName = "<" + indexPrefix + "-{now/d}-00001>";
                    // ensure the index exists (template, policy and aliases)
                    opensearchV3IndexManager.ensureIndexExists(indexName, aliasWrite, aliasRead, indexPrefix, indexMetadata.getTtl());

                    // index requests — split into batches to avoid 413
                    List<List<AuditReport>> batches = partition(auditIndexRequestsGrouped, bulkBatchSize);
                    try {
                        BulkResponse lastResponse = null;
                        for (List<AuditReport> batch : batches) {
                            BulkRequest bulkRequest = BulkRequest.of(b -> b
                                    .operations(batch.stream()
                                            .map(auditIndexRequest -> toBulkOperation(auditIndexRequest, aliasWrite))
                                            .toList())
                            );
                            lastResponse = bulkRequest(bulkRequest);
                        }
                        log.debug("indexing {} for {} took {} ms", auditIndexRequestsGrouped.size(), indexMetadata, System.currentTimeMillis() - now);
                        if (auditStarter && lastResponse != null) {
                            auditResponseOk(lastResponse);
                        }

                    } catch (Exception e) {
                        log.error("Error indexing for index {}", indexMetadata, e);
                        if (auditStarter) {
                            auditResponseFailed(e);
                        }
                    } finally {
                        if (auditStarter) {
                            auditorService.stopAuditAndNotify();
                        }
                    }
                });
    }

    private boolean startAudit(List<AuditReport> auditReports) {
        if (! skipAudit(auditReports)) {
            auditorService.startAudit();
            auditorKeyValueService.addIndexedKeyValues(List.of(
                    IndexedKeyValue.builder().key("httpMethod").value("bulk_index").build(),
                    IndexedKeyValue.builder().key("requestUri").value("/opensearch").build()
            ));
            return true;
        } else {
            return false;
        }
    }

    private void auditResponseOk(BulkResponse bulkResponse) {
        int status = 200; // OpenSearch client doesn't expose HTTP status for successful operations
        auditorKeyValueService.addIndexedKeyValues(List.of(
                IndexedKeyValue.builder().key("httpStatus").value(""+status).valueInteger(status).build(),
                IndexedKeyValue.builder().key("itemsSize").valueInteger(bulkResponse.items().size()).build(),
                IndexedKeyValue.builder().key("indexDuration").value(""+bulkResponse.took()).build(),
                IndexedKeyValue.builder().key("ingestDuration").value(""+bulkResponse.ingestTook()).build(),
                IndexedKeyValue.builder().key("hasFailures").value(""+bulkResponse.errors()).build()
        ));
        auditorEvents.addAuditEvents(WebAuditReportData.TYPE, List.of(
                WebAuditEvent.builder()
                        .httpAuditedRequest(HttpAuditedRequest.builder()
                                .body("{}")
                                .method("bulk_index")
                                .build())
                        .httpAuditedResponse(HttpAuditedResponse.builder()
                                .body(bulkResponse.errors() ? "Has failures" : "Success")
                                .status(200)
                                .build())
                        .build()
        ));
    }

    private void auditResponseFailed(Exception e) {
        auditorKeyValueService.addIndexedKeyValues(List.of(
                IndexedKeyValue.builder().key("httpStatus").value("500").valueInteger(200).build()
        ));

        auditorEvents.addAuditEvents(WebAuditReportData.TYPE, List.of(
                WebAuditEvent.builder()
                        .httpAuditedRequest(HttpAuditedRequest.builder()
                                .body("{}")
                                .method("bulk_index")
                                .uri("opensearch_cluster")
                                .build())
                        .httpAuditedResponse(HttpAuditedResponse.builder()
                                .status(500)
                                .body(e.getMessage())
                                .build())
                        .build()
        ));
    }

    /**
     * Do not audit "sef" reports, i.e reports from this application
     * @param auditReports
     * @return
     */
    private boolean skipAudit(List<AuditReport> auditReports) {
        return auditReports.isEmpty() ||
                auditReports.iterator().next().getAppId().equals(sdkApplicationProperties.getAppId());
    }

    private static <T> List<List<T>> partition(List<T> list, int size) {
        return IntStream.range(0, (list.size() + size - 1) / size)
                .mapToObj(i -> list.subList(i * size, Math.min((i + 1) * size, list.size())))
                .toList();
    }

    @SneakyThrows
    private BulkResponse bulkRequest(BulkRequest bulkRequest) {
        BulkResponse bulkResponse = openSearchClient.bulk(bulkRequest);
        if (bulkResponse.errors()) {
            bulkResponse.items().stream()
                    .filter(item -> item.error() != null)
                    .forEach(item -> log.error("Bulk item in error {}", item.error().reason()));
        }
        return bulkResponse;
    }

    @SneakyThrows
    private BulkOperation toBulkOperation(AuditReport auditReport, String writeAlias) {
        String documentId = isNull(auditReport.getId()) ? auditReport.getRequestId() : auditReport.getId();
        JsonNode jsonNode = objectMapper.valueToTree(auditReport);

        return BulkOperation.of(op -> op
                .index(IndexOperation.of(idx -> idx
                        .index(writeAlias)
                        .id(documentId)
                        .document(jsonNode)
                ))
        );
    }

}
