package org.kockpit.audit.backend.services;

import com.azure.core.util.Context;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.models.SearchOptions;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.kockpit.audit.backend.BackendApiDelegate;
import org.kockpit.audit.backend.ConfigEnvsInner;
import org.kockpit.audit.backend.Page;
import org.kockpit.audit.backend.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuditReportService implements BackendApiDelegate {

    private final SearchClient client;

    private final ObjectMapper objectMapper;

    @Value("${kockpit.audit.azure.search.index_name}")
    private String indexName;

    @Value("${kockpit.audit.azure.search.max_size:25}")
    private Integer maxSize;

    @Override
    public ResponseEntity<List<ConfigEnvsInner>> listEnvironments() {
        SearchOptions searchOptions = new SearchOptions()
                .setSelect("domain", "env");

        List<ConfigEnvsInner> list = client.search("*", searchOptions, Context.NONE)
                .stream()
                .map(searchResult -> searchResult.getDocument(ConfigEnvsInner.class))
                .toList();

        return ResponseEntity.ok(list);
    }

    @Override
    public ResponseEntity<Page> searchAudits(String query) {
        SearchOptions searchOptions = new SearchOptions()
                .setOrderBy("start desc")
                .setTop(maxSize);

        List<SearchAuditReport> list = client.search(query, searchOptions, Context.NONE)
                .stream()
                .map(searchResult -> searchResult.getDocument(SearchAuditReport.class))
                .toList();

        return ResponseEntity.ok(Page.builder()
                .items(new ArrayList<>(list))
                .size(maxSize.longValue())
                .totalCount(count())
                .build());
    }

    @Override
    public ResponseEntity<Page> listAudits(Integer start, Integer size) {
        return ResponseEntity.ok(Page.builder()
                        .size(size.longValue())
                        .totalCount(count())
                        .items(new ArrayList<>(getAll(size, start)))
                .build());
    }

    @Override
    public ResponseEntity<Page> listAuditsDomainEnv(String domain, String env) {
        SearchOptions searchOptions = new SearchOptions()
                .setOrderBy("start desc")
                .setSearchFields("domain:%s".formatted(domain), "env:%s".formatted(env))
                .setTop(maxSize);

        List<SearchAuditReport> list = client.search("*", searchOptions, Context.NONE)
                .stream()
                .map(searchResult -> searchResult.getDocument(SearchAuditReport.class))
                .toList();

        return ResponseEntity.ok(Page.builder()
                        .items(new ArrayList<>(list))
                        .size(maxSize.longValue())
                        .totalCount(count())
                .build());
    }

    @Override
    public ResponseEntity<Object> auditReportById(String id) {
        Object document = client.getDocument(id, SearchAuditReport.class);
        return ResponseEntity.ok(document);
    }

    @Override
    public ResponseEntity<List<Object>> listAuditReports() {
        List<Object> list = new ArrayList<>(getAll(maxSize, 0));
        return ResponseEntity.ok(list);
    }

    private List<SearchAuditReport> getAll(Integer size, Integer start) {
        SearchOptions searchOptions = new SearchOptions()
                .setSkip(start)
                .setOrderBy("start desc")
                .setTop(Math.min(maxSize, size));

        return client.search("*", searchOptions, Context.NONE)
                .stream()
                .map(searchResult -> searchResult.getDocument(SearchAuditReport.class))
                .toList();
    };

    private SearchAuditReport getById(String id) {
        return client.getDocument(id, SearchAuditReport.class);
    }

    /*
    public List<AuditReportSummary> getReportsSummaries() {
        return getAll().stream().map(report ->
                new AuditReportSummary(
                        report.getId(),
                        report.getDomain(),
                        report.getEnv(),
                        Instant.ofEpochMilli(report.getStart()),
                        report.getAppId(),
                        report.getRequestId(),
                        report.getTtl(),
                        status(report.getIndexedKeyValues())
                )
        ).toList();
    }
     */

    private Integer status(List<SearchIndexedKeyValue> indexedKeyValues) {
        if (CollectionUtils.isEmpty(indexedKeyValues)) {
            return null;
        } else {
            return indexedKeyValues.stream()
                    .filter(kv -> kv.getKey().equals("httpStatus"))
                    .findFirst()
                    .map(SearchIndexedKeyValue::getValueInteger)
                    .orElse(null);
        }
    }

    public List<HttpRequestSummary> getHttpRequests(String id) {
        var report = getById(id);
        return report.getAudits().stream()
                .filter(searchAudit -> searchAudit.getType().equals("builtin.web"))
                .map(searchAudit -> {
                    // fixme
                    String s = searchAudit.getEvents().get(0);
                    Map<String, Object> o = toMap(s);

                    Map<String, Object> httpAuditedRequest = (Map<String, Object>) o.get("httpAuditedRequest");
                    Map<String, Object> httpAuditedResponse = (Map<String, Object>) o.get("httpAuditedResponse");

                    return new HttpRequestSummary(
                            (String) httpAuditedRequest.get("method"),
                            (String) httpAuditedRequest.get("uri"),
                            (int) httpAuditedResponse.get("status"),
                            report.getEnd() - report.getStart(),
                            Instant.ofEpochMilli(report.getStart()),
                            report.getRequestId()
                    );
                }).toList();
    }

    @Override
    public ResponseEntity<Object> auditReportRequestsByIdAndTraceId(String id, String traceId) {
        var report = getById(id);
        HttpExchangeAudit httpExchangeAudit = report.getAudits().stream()
                .filter(searchAudit -> searchAudit.getType().equals("builtin.web"))
                .map(searchAudit -> {
                    String s = searchAudit.getEvents().get(0);
                    Map<String, Object> o = toMap(s);

                    Map<String, Object> httpAuditedRequest = (Map<String, Object>) o.get("httpAuditedRequest");
                    Map<String, Object> httpAuditedResponse = (Map<String, Object>) o.get("httpAuditedResponse");

                    HttpExchangeAudit build = HttpExchangeAudit.builder()
                            .httpAuditedRequest(
                                    HttpAuditedRequest.builder()
                                            .headers(headers(httpAuditedRequest))
                                            .uri((String) httpAuditedRequest.get("uri"))
                                            .method((String) httpAuditedRequest.get("method"))
                                            .body((String) httpAuditedRequest.get("body"))
                                            .params((Map<String, List<String>>) httpAuditedRequest.get("params"))
                                            .build()
                            )
                            .httpAuditedResponse(HttpAuditedResponse.builder()
                                    .headers(headers(httpAuditedResponse))
                                    .payload((String) httpAuditedResponse.get("body"))
                                    .status((int) httpAuditedResponse.get("status"))
                                    .build())
                            .build();
                    build.setStartTime(report.getStart());
                    build.setEndTime(report.getEnd());
                    return build;
                }).findFirst().orElse(null);

        return ResponseEntity.ok(httpExchangeAudit);
    }

    @SneakyThrows
    private Map<String, Object> toMap(String s) {
        TypeReference<HashMap<String,Object>> typeRef = new TypeReference<>() {};
        return objectMapper.readValue(s, typeRef);
    }

    private HttpHeaders headers(Map<String, Object> input) {
        Map<String, List<String>> headers = (Map<String, List<String>>) input.get("headers");
        if (CollectionUtils.isEmpty(headers)) {
            return new HttpHeaders();
        }
        return new HttpHeaders(new LinkedMultiValueMap<>(headers));
    }

    private Long count() {
        return client.getDocumentCount();
    }
}
