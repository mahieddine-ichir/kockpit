package org.kockpit.audit.stream.opensearch;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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
import org.opensearch.client.opensearch.core.BulkResponse;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.isNull;

@Slf4j
@RequiredArgsConstructor
public class AuditConsumerForOpensearch implements AuditConsumer {

    // local cache for batch indexing
    private final Queue<byte[]> auditReports = new ConcurrentLinkedQueue<>();

    private final OpensearchIndexer opensearchIndexer;

    private final AuditorService auditorService;

    private final AuditorKeyValueService auditorKeyValueService;

    private final AuditorEventService auditorEvents;

    private final SdkApplicationProperties sdkApplicationProperties;

    private final Integer ttlDefaultInDays;

    private final Integer batchSize;

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

        List<byte[]> batch = new ArrayList<>();
        byte[] report;
        for (int i = 0; i < batchSize && (report = auditReports.poll()) != null; i++) {
            batch.add(report);
        }
        this.indexAudits(batch);
    }

    private void indexAudits(List<byte[]> auditReports) {
        Map<IndexMetadata, List<AuditReport>> collect = auditReports.stream()
                .map(AuditStreamJson::readAuditReport)
                .peek(auditReport -> {
                    if (isNull(auditReport.getTtl())) {
                        auditReport.setTtl(ttlDefaultInDays);
                    }
                })
                .collect(Collectors.groupingBy(IndexMetadata::of));

        collect.forEach((indexMetadata, auditReports1) -> {
            boolean auditStarter = startAudit(auditReports1);
            opensearchIndexer.index(auditReports1, indexMetadata);
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
                IndexedKeyValue.builder().key("httpStatus").value("500").valueInteger(500).build()
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
}
