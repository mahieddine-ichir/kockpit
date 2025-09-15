package org.kockpit.audit.stream.opensearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.kockpit.audit.stream.api.model.AuditReport;
import org.kockpit.audit.stream.opensearch.model.SearchAuditReport;
import org.opensearch.action.bulk.BulkItemResponse;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class OpensearchIndexer implements AuditConsumer {

    private final List<AuditReport> auditReports = new ArrayList<>();

    private final RestHighLevelClient restHighLevelClient;

    private final AuditReportMapper auditReportMapper;

    private final ObjectMapper objectMapper;

    @Value("${kockpit.audit.stream.opensearch.index_name}")
    private String index;

    @PostConstruct
    void initIndex() {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("{}", throwable.getMessage(), throwable);
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
        log.debug("Start indexing {} reports", auditReports.size());
        Long now = System.currentTimeMillis();
        // defensive copy
        AuditReport[] copy = Arrays.copyOf(auditReports.toArray(), auditReports.size(), AuditReport[].class);
        auditReports.clear();

        BulkRequest request = Arrays.stream(copy)
                .map(auditReportMapper::map)
                .map(this::toIndexRequest)
                .collect(BulkRequest::new, BulkRequest::add, (bulkRequest, bulkRequest2) -> bulkRequest.add(bulkRequest2.requests()));

        bulkRequest(request);
        log.debug("indexing took {} ms", System.currentTimeMillis() - now);
    }

    @SneakyThrows
    private void bulkRequest(BulkRequest bulkRequest) {
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        if (bulkResponse.hasFailures()) {
            Stream.of(bulkResponse.getItems())
                    .filter(BulkItemResponse::isFailed)
                    .forEach(bulkItemResponse -> log.error("Bulk item in error {}", bulkItemResponse.getFailureMessage()));
        }
    }

    @SneakyThrows
    private IndexRequest toIndexRequest(SearchAuditReport searchAuditReport) {
        return new IndexRequest(index).source(objectMapper.writeValueAsBytes(searchAuditReport), XContentType.JSON);
    }

}
