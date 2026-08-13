package org.kockpit.audit.stream.opensearch;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.kockpit.audit.stream.api.model.AuditReport;
import org.kockpit.sdk.SdkApplicationProperties;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.BulkResponseItem;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;
import org.opensearch.client.util.ObjectBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.kockpit.audit.stream.opensearch.OpensearchHelper.*;

@Slf4j
@RequiredArgsConstructor
public class AuditConsumerForOpensearch implements AuditConsumer {

    // local cache for batch indexing
    private final List<AuditReport> auditReports = new ArrayList<>();

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
    public void accept(AuditReport auditReport) {
        auditReports.add(auditReport);
    }

    @Scheduled(
            fixedDelayString = "${kockpit.audit.stream.opensearch.scheduler_ms:5000}",
            timeUnit = TimeUnit.MILLISECONDS)
    void index() {
        if (auditReports.isEmpty()) {
            return;
        }
        // defensive copy
        AuditReport[] copy = Arrays.copyOf(auditReports.toArray(), auditReports.size(), AuditReport[].class);
        auditReports.clear();

        this.indexAudits(copy);
    }

    private void indexAudits(AuditReport[] auditReports) {
        Arrays.stream(auditReports)
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
                            // A single unparseable field makes OpenSearch reject the whole document.
                            // Rather than losing the audit, re-index the failed ones in a degraded form.
                            if (lastResponse.errors()) {
                                retryFailedItemsDegraded(lastResponse, batch, aliasWrite);
                            }
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

    /**
     * When a bulk response contains per-item failures, OpenSearch has rejected each failing
     * document entirely (a single unparseable field is enough). To avoid losing the audit, we
     * re-index every failed document in a degraded but always-indexable form (see
     * {@link #toDegradedBulkOperation}). Bulk items are positional to the request operations,
     * so they line up with the batch that produced them.
     */
    private void retryFailedItemsDegraded(BulkResponse response, List<AuditReport> batch, String writeAlias) {
        List<BulkResponseItem> items = response.items();
        List<BulkOperation> retryOperations = new ArrayList<>();
        for (int i = 0; i < items.size() && i < batch.size(); i++) {
            BulkResponseItem item = items.get(i);
            if (item.error() != null) {
                retryOperations.add(toDegradedBulkOperation(batch.get(i), writeAlias, item.error().reason()));
            }
        }
        if (retryOperations.isEmpty()) {
            return;
        }
        log.warn("Re-indexing {} audit document(s) in degraded form after an indexing failure", retryOperations.size());
        BulkResponse retryResponse = bulkRequest(BulkRequest.of(b -> b.operations(retryOperations)));
        if (retryResponse.errors()) {
            long stillFailing = retryResponse.items().stream().filter(item -> item.error() != null).count();
            log.error("{} audit document(s) still failed after degraded re-indexing and were dropped", stillFailing);
        }
    }

    /**
     * Builds a degraded index operation for a document OpenSearch refused: all top-level (mapped)
     * fields are kept, while the free-form {@code audits} sub-tree — the only part that can carry
     * unmapped/conflicting values — is serialized into a single {@code auditsRaw} string field.
     * The failure reason is stored under {@code indexingError} (no leading underscore, which
     * OpenSearch rejects for dynamically mapped fields). The audit is thus preserved and
     * inspectable, at the cost of structured search over its events.
     */
    @SneakyThrows
    private BulkOperation toDegradedBulkOperation(AuditReport auditReport, String writeAlias, String reason) {
        String documentId = isNull(auditReport.getId()) ? auditReport.getRequestId() : auditReport.getId();
        ObjectNode node = (ObjectNode) objectMapper.valueToTree(auditReport);
        JsonNode audits = node.remove("audits");
        if (audits != null) {
            node.put("auditsRaw", audits.toString());
        }
        node.put("indexingError", reason);

        return BulkOperation.of(op -> op
                .index(IndexOperation.of(idx -> idx
                        .index(writeAlias)
                        .id(documentId)
                        .document(node)
                ))
        );
    }

}
