package com.example.kockpitbackendserviceopensearch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.mapping.DynamicMapping;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenSearchInitializer {

    private final OpenSearchClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${kockpit.audit.opensearch.index}")
    private String indexName;
    @PostConstruct
    public void init() {
        try {
            boolean exists = client.indices()
                    .exists(ExistsRequest.of(e -> e.index(indexName)))
                    .value();

            if (!exists) {
                log.info("infex {} does not exist we will creat ", indexName);
                client.indices().create(CreateIndexRequest.of(c -> c
                        .index(indexName)
                        .mappings(m -> m
                                .properties("domain", p -> p.keyword(k -> k))
                                .properties("env", p -> p.keyword(k -> k))
                                .properties("start", p -> p.date(d -> d))
                                .properties("message", p -> p.text(t -> t))
                                .properties("audits", a -> a.nested(n -> n
                                        .properties("type", p -> p.keyword(k -> k))
                                        .properties("events", p -> p.keyword(k -> k))
                                ))
                        )
                ));

                InputStream is = new ClassPathResource("audits.json").getInputStream();
                List<Map<String, Object>> audits = objectMapper.readValue(is, new TypeReference<>() {});
                // normalize the audits data
                for (Map<String, Object> audit : audits) {
                    audit.remove("indexedKeyValuesComputeFunctions");
                    if (audit.containsKey("audits")) {
                        List<Map<String, Object>> auditList = (List<Map<String, Object>>) audit.get("audits");
                        for (Map<String, Object> auditItem : auditList) {
                            if (auditItem.containsKey("events")) {
                                List<Map<String, Object>> events = (List<Map<String, Object>>) auditItem.get("events");
                                List<String> parsedEvents = new ArrayList<>();
                                for (Object event : events) {
                                    if (event instanceof Map) {
                                        Map<String, Object> eventMap = (Map<String, Object>) event;
                                        if (eventMap.containsKey("httpAuditedRequest")) {
                                            Map<String, Object> req = (Map<String, Object>) eventMap.get("httpAuditedRequest");
                                            Object body = req.get("body");
                                            if (!(body instanceof Map)) {
                                                try {
                                                    Map<String, Object> parsed = objectMapper.readValue(body.toString(), new TypeReference<>() {});
                                                    req.put("body", parsed);
                                                } catch (Exception ex) {
                                                    req.put("body", Map.of("raw", body.toString()));
                                                }
                                            }
                                        }
                                        if (eventMap.containsKey("httpAuditedResponse")) {
                                            Map<String, Object> resp = (Map<String, Object>) eventMap.get("httpAuditedResponse");
                                            Object body = resp.get("body");
                                            if (!(body instanceof Map)) {
                                                try {
                                                    Map<String, Object> parsed = objectMapper.readValue(body.toString(), new TypeReference<>() {});
                                                    resp.put("body", parsed);
                                                } catch (Exception ex) {
                                                    resp.put("body", Map.of("raw", body.toString()));
                                                }
                                            }
                                        }
                                        try {
                                            String json = objectMapper.writeValueAsString(eventMap);
                                            parsedEvents.add(json);
                                        } catch (Exception ex) {
                                            log.error("serialize event failedddd", ex);
                                        }
                                    }
                                }
                                auditItem.put("events", parsedEvents);


                            }
                        }
                    }
                }

                log.info("loaded {} records from audits.json", audits.size());

                BulkRequest.Builder br = new BulkRequest.Builder();
                for (Map<String, Object> audit : audits) {
                    String documentId = (String) audit.get("id");
                    br.operations(op -> op.index(idx -> idx
                            .index(indexName)
                            .id(documentId)
                            .document(audit)
                    ));
                }

                BulkResponse response = client.bulk(br.build());
                if (response.errors()) {
                    log.error("indexing had errors!");
                    response.items().forEach(item -> {
                        if (item.error() != null) {
                            log.error("error status={} reason={}", item.status(), item.error().reason());
                        }
                    });
                } else {
                    log.info("successfully indexed {} documents into {}", audits.size(), indexName);
                }
            } else {
                log.info("index {} already exists", indexName);
            }
        } catch (Exception e) {
            log.error("initialize opendearch failed", e);
        }
    }
}
