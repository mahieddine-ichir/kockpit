package org.kockpit.backend.services.dashboard;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.*;
import org.opensearch.search.aggregations.Aggregation;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final RestHighLevelClient client;

    private final RestClient restClient;

    /**
     * Mapper auto-configure par Boot : cette lecture n'a aucun contrat de format a preserver
     * (agregations OpenSearch lues en Map), contrairement aux mappers dedies du projet.
     */
    private final ObjectMapper objectMapper;

    @Value("${kockpit.backend.opensearch.index}")
    private String index;

    // Must mirror kockpit.audit.stream.opensearch.wrap_indexed_key_values on the writer side, like
    // the search (OpensearchRepository): wrapped indices map the key/values nested under
    // indexedExtensions[], not at the root - aggregating on the wrong path matches nothing, which
    // left the HTTP status chart blank and avgDuration null.
    @Value("${kockpit.backend.opensearch.wrap_indexed_key_values:false}")
    private boolean wrapIndexedKeyValues;

    @SneakyThrows
    Map<String, List<Object>> appDetails(String domain, String env) {

        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("apps_distribution")
                .field("appId").size(20);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .size(0)
                .aggregation(termsAggregationBuilder);

        SearchRequest searchRequest = new SearchRequest()
                .source(searchSourceBuilder)
                .indices(getAuditAliasName(domain, "audit-data", env));

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        Aggregation first = searchResponse.getAggregations().asList().get(0);

        Map<String, List<Object>> aggs = new HashMap<>();
        aggs.put(first.getName(), new ArrayList<>());
        if (first instanceof ParsedStringTerms parsedStringTerms) {
            parsedStringTerms.getBuckets().forEach(bucket -> aggs.get(first.getName())
                    .add(Map.of("key", bucket.getKeyAsString(), "docCount", bucket.getDocCount())));
        }
        return aggs;
    }

    @SneakyThrows
    List<Map<String, Object>> avgDurationByApp(String domain, String env, String gte) {
        Map byApp = (Map) runJson(domain, env, "/appCountByApp.json", Map.of("--gte--", gte)).get("by_app");
        List<Map> buckets = (List<Map>) byApp.get("buckets");
        Map<String, Double> avgDurations = avgDurationsByApp(domain, env, gte);
        return buckets.stream()
                .map(map -> {
                    String name = readMap(map, "key").toString();
                    // Map.of() rejects null values (no average for this app), a plain HashMap doesn't.
                    Map<String, Object> ret = new HashMap<>();
                    ret.put("name", name);
                    ret.put("count", readMap(map, "doc_count"));
                    ret.put("avgDuration", avgDurations.get(name));
                    return ret;
                }).toList();
    }

    // Separate from the counts, and allowed to fail: averaging needs doc values on valueInteger,
    // which some indices map doc_values:false - OpenSearch then rejects the whole request. In one
    // request with the counts, that blanked "Request Distribution by Application" too.
    private Map<String, Double> avgDurationsByApp(String domain, String env, String gte) {
        try {
            Map byApp = (Map) runJson(domain, env, "/avgDurationByApp.json",
                    Map.of("--gte--", gte, "--kv--", indexedKeyValuesPath())).get("by_app");
            Map<String, Double> avgDurations = new HashMap<>();
            for (Map bucket : (List<Map>) byApp.get("buckets")) {
                Object avg = readMap(bucket, "avg_duration.filter_duration.avg_value.value");
                if (avg instanceof Number number) {
                    avgDurations.put(readMap(bucket, "key").toString(), number.doubleValue());
                }
            }
            return avgDurations;
        } catch (Exception e) {
            log.warn("Average duration unavailable for {}/{} (index can't aggregate duration values): {}",
                    domain, env, e.getMessage());
            return Map.of();
        }
    }

    @SneakyThrows
    List<Map<String, Object>> statusDistributionByAppId(String domain, String env, String gte) {
        log.trace("statusDistributionByAppId({})", gte);
        Map statusNested = (Map) runJson(domain, env, "/statusDistributionByAppId.json", Map.of("--gte--", gte, "--kv--", indexedKeyValuesPath())).get("by_app");
        List<Map> buckets = (List<Map>) readMap(statusNested, "buckets");

        return buckets.stream()
                .map(map -> {
                    Map<String, Object> ret = new HashMap<>();
                    ret.put("name", map.get("key"));
                    // A "filters" aggregation (one range query per class), not a "range" one:
                    // valueInteger is mapped doc_values:false on some indices, which range/avg
                    // aggregations need but range queries don't. Its buckets come keyed by name.
                    Map<String, Map> subBuckets = (Map<String, Map>) readMap(map, "http_status_nested.filter_status.status_groups.buckets");
                    Stream.of("2xx", "3xx", "4xx", "5xx").forEach(status -> {
                        Map subBucket = subBuckets == null ? null : subBuckets.get(status);
                        if (subBucket != null) {
                            ret.put(status, subBucket.get("doc_count"));
                        }
                    });
                    return ret;
                }).toList();
    }

    @SneakyThrows
    List<Map<String, Object>> overTimeByAppId(String domain, String env, String gte) {
        log.trace("overTimeByAppId({})", gte);
        Map statusNested = (Map) runJson(domain, env, "/overTimeByAppId.json", Map.of("--gte--", gte)).get("over_time");
        List<Map> buckets = (List<Map>) readMap(statusNested, "buckets");

        return buckets.stream()
                .map(map -> {
                    Map<String, Object> ret = new HashMap<>();
                    ret.put("date", map.get("key"));
                    ((List<Map>) readMap(map, "by_app.buckets")).stream()
                            .forEach(subBucket -> ret.put(subBucket.get("key").toString(), readMap(subBucket, "doc_count")));
                    return ret;
                }).toList();
    }

    private Map<String, Object> runJson(String domain, String env, String json, @Nullable Map<String, Object> replacements) throws IOException {
        String entity = new String(this.getClass().getResourceAsStream(json).readAllBytes());
        if (replacements != null) {
            for (Map.Entry<String, Object> entry : replacements.entrySet()) {
                entity = entity.replace(entry.getKey(), entry.getValue().toString());
            }
        }

        Request request = new Request("POST", "/%s/_search".formatted(getAuditAliasName(domain, index, env)));
        request.setJsonEntity(entity);

        Response response = restClient.performRequest(request);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        InputStream content = response.getEntity().getContent();
        StreamUtils.copy(content, os);
        content.close();
        return (Map) objectMapper.readValue(new String(os.toByteArray()), Map.class)
                .get("aggregations");
    }


    String indexedKeyValuesPath() {
        return wrapIndexedKeyValues ? "indexedExtensions.indexedKeyValues" : "indexedKeyValues";
    }

    public static String getAuditAliasName(String domain, String indexName, String env) {
        return domain + "-" + indexName + "-" + env + "-read".toLowerCase();
    }

    Object readMap(Map<String, Object> input, String key) {
        if (key.contains(".")) {
            String key1 = key.substring(0, key.indexOf("."));
            Object object = input.get(key1);
            if (object instanceof Map map) {
                return readMap(map, key.substring(key.indexOf(".") + 1));
            } else {
                return object;
            }
        } else {
            return input.get(key);
        }
    }

}
