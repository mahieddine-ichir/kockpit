package com.example.insightsstreamingapp.config;

import com.example.insightsstreamingappapi.InsightDocument;
import com.example.insightsstreamingappapi.InsightStats;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.kockpit.audit.stream.api.AuditReport;
import org.kockpit.audit.stream.api.IndexedKeyValue;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class OpenSearchInsightsConsumer implements AuditConsumer {

    private final RestHighLevelClient client;
    private final String indexName;
    private final ObjectMapper objectMapper;
    private final List<AuditReport> buffer = new CopyOnWriteArrayList<>();

    public OpenSearchInsightsConsumer(RestHighLevelClient client, String indexName, ObjectMapper objectMapper) {
        this.client = client;
        this.indexName = indexName;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() throws IOException {
        initIndex();
    }

    private void initIndex() throws IOException {
        if (!client.indices().exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT)) {
            CreateIndexRequest request = new CreateIndexRequest(indexName);


            String mapping = "{\n" +
                    "  \"properties\": {\n" +
                    "    \"timestamp\": {\"type\": \"date\"},\n" +
                    "    \"stats\": {\n" +
                    "      \"type\": \"object\",\n" +
                    "      \"properties\": {\n" +
                    "        \"totalRequests\": {\"type\": \"long\"},\n" +
                    "        \"successRate\": {\"type\": \"float\"},\n" +
                    "        \"errorRate\": {\"type\": \"float\"},\n" +
                    "        \"avgDuration\": {\"type\": \"float\"},\n" +
                    "        \"countsPerDomainEnv\": {\"type\": \"object\"},\n" +
                    "        \"statusDistribution\": {\"type\": \"object\"},\n" +
                    "        \"methodDistribution\": {\"type\": \"object\"},\n" +
                    "        \"avgDurationByStatus\": {\"type\": \"object\"}\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";

            request.mapping(mapping, XContentType.JSON);
            client.indices().create(request, RequestOptions.DEFAULT);
            log.info("created index '{}'", indexName);
        }
    }

    @Override
    public void start() {
        log.info("openSearchconsumer started");
    }

    @Override
    public void stop() {
        log.info("openSearchconsumer stopped");
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("error in insigt consumer", throwable);
    }

    public void accept(List<AuditReport> auditReports) {
        buffer.addAll(auditReports);
        log.debug("added {} reports, buffer size: {}", auditReports.size(), buffer.size());
        if (buffer.size() >= 50) {
            aggregateAndIndex();
        }
    }

    @Override
    public void accept(AuditReport auditReport) {
        log.info("received audit: {} from {}/{}",
                auditReport.getId(), auditReport.getDomain(), auditReport.getEnv());
        buffer.add(auditReport);
        if (buffer.size() >= 10) {
            aggregateAndIndex();
        }
    }

    @Scheduled(fixedRate = 5000)
    public void flushBuffer() {
        if (!buffer.isEmpty()) {
            log.info("flushing buffer with {} items", buffer.size());
            aggregateAndIndex();
        }
    }

    private void aggregateAndIndex() {
        if (buffer.isEmpty()) {
            log.debug("bufferempty nothing to aggregate");
            return;
        }

        log.debug("starting aggregation of {} reports", buffer.size());

        Map<String, Long> countsPerDomainEnv = new HashMap<>();
        Map<String, Long> statusCounts = new HashMap<>();
        Map<String, Long> methodCounts = new HashMap<>();
        Map<String, Long> statusDurationSums = new HashMap<>();
        Map<String, Long> statusRequestCounts = new HashMap<>();

        long totalDuration = 0;
        int successCount = 0;
        int totalRequests = buffer.size();

        for (AuditReport report : buffer) {
            try {
                String domainEnvKey = report.getDomain() + "|" + report.getEnv();
                countsPerDomainEnv.merge(domainEnvKey, 1L, Long::sum);

                String httpStatus = null;
                String httpMethod = null;
                Integer duration = null;
                if (report.getIndexedKeyValues() != null) {
                    for (IndexedKeyValue kv : report.getIndexedKeyValues()) {
                        if (kv.getKey() == null) continue;

                        switch (kv.getKey()) {
                            case "httpStatus":
                                httpStatus = kv.getValue();
                                if (httpStatus != null) {
                                    statusCounts.merge(httpStatus, 1L, Long::sum);
                                    try {
                                        int statusCode = Integer.parseInt(httpStatus);
                                        if (statusCode >= 200 && statusCode < 300) {
                                            successCount++;
                                        }
                                    } catch (NumberFormatException e) {
                                        log.warn("invalid http status code: {}", httpStatus);
                                    }
                                }
                                break;

                            case "httpMethod":
                                httpMethod = kv.getValue();
                                if (httpMethod != null) {
                                    methodCounts.merge(httpMethod, 1L, Long::sum);
                                }
                                break;

                            case "duration":
                                if (kv.getValueInteger() != null) {
                                    duration = kv.getValueInteger();
                                } else if (kv.getValue() != null) {
                                    try {
                                        duration = Integer.parseInt(kv.getValue());
                                    } catch (NumberFormatException e) {
                                        log.warn("invalid duration value: {}", kv.getValue());
                                    }
                                }

                                if (duration != null) {
                                    totalDuration += duration;
                                }
                                break;
                        }
                    }

                    if (httpStatus != null && duration != null) {
                        statusDurationSums.merge(httpStatus, (long) duration, Long::sum);
                        statusRequestCounts.merge(httpStatus, 1L, Long::sum);
                    }
                } else {
                    log.warn("report {} has no indexed key values", report.getId());
                }
            } catch (Exception e) {
                log.error("error processing report {}: {}", report.getId(), e.getMessage(), e);
            }
        }
        float successRate = totalRequests > 0 ? (float) successCount / totalRequests * 100 : 0;
        float errorRate = 100 - successRate;
        float avgDuration = totalRequests > 0 ? (float) totalDuration / totalRequests : 0;

        Map<String, Float> avgDurationByStatus = new HashMap<>();
        for (String status : statusDurationSums.keySet()) {
            long sum = statusDurationSums.get(status);
            long count = statusRequestCounts.get(status);
            if (count > 0) {
                avgDurationByStatus.put(status, (float) sum / count);
            }
        }
        log.debug("aggregated stats: totalRequests={}, successCount={}, successRate={}%, errorRate={}%, avgDuration={}ms",
                totalRequests, successCount, successRate, errorRate, avgDuration);
        log.debug("domain/env distribution: {}", countsPerDomainEnv);
        log.debug("status distribution: {}", statusCounts);
        log.debug("method distribution: {}", methodCounts);
        log.debug("avg duration by status: {}", avgDurationByStatus);

        InsightStats stats = InsightStats.builder()
                .totalRequests(totalRequests)
                .successRate(successRate)
                .errorRate(errorRate)
                .avgDuration(avgDuration)
                .countsPerDomainEnv(countsPerDomainEnv)
                .statusDistribution(statusCounts)
                .methodDistribution(methodCounts)
                .avgDurationByStatus(avgDurationByStatus)
                .build();

        InsightDocument doc = InsightDocument.builder()
                .id(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .stats(stats)
                .build();

        try {
            IndexRequest request = new IndexRequest(indexName)
                    .id(doc.getId())
                    .source(objectMapper.writeValueAsString(doc), XContentType.JSON);
            client.index(request, RequestOptions.DEFAULT);

            log.info("sccessfully indexed dashboard insight");
        } catch (IOException e) {
            log.error("failed to index insight: {}", e.getMessage(), e);
        }

        buffer.clear();
    }
}