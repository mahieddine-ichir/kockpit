package org.kockpit.backend.services.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final RestHighLevelClient client;

    private final RestClient restClient;

    //private final String index;

    @SneakyThrows
    Map<String, List<Object>> appDetails() {

        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("apps_distribution")
                .field("appId").size(20);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .size(0)
                .aggregation(termsAggregationBuilder);

        SearchRequest searchRequest = new SearchRequest()
                .source(searchSourceBuilder)
                .indices(getAuditAliasName("rcu", "audit-data", "dev"));

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
    List<Map<String, Object>> avgDurationByApp() {
        Map byApp = (Map) runJson("/avgDurationByApp.json", null).get("by_app");
        List<Map> buckets = (List<Map>) byApp.get("buckets");

        return buckets.stream()
                .map(map -> {
                    Map avgDuration = (Map) map.get("avg_duration");
                    Map filterDuration = (Map) avgDuration.get("filter_duration");
                    Map avgValue = (Map) filterDuration.get("avg_value");
                    return Map.of(
                            "name", map.get("key"),
                            "count", map.get("doc_count"),
                            "avgDuration", avgValue.get("value")
                    );
                }).toList();
    }

    @SneakyThrows
    List<Map<String, Object>> statusDistributionByAppId(String gte) {
        log.trace("statusDistributionByAppId({})", gte);
        Map statusNested = (Map) runJson("/statusDistributionByAppId.json", Map.of("--gte--", gte)).get("by_app");
        List<Map> buckets = (List<Map>) readMap(statusNested, "buckets");

        return buckets.stream()
                .map(map -> {
                    Map<String, Object> ret = new HashMap<>();
                    ret.put("name", map.get("key"));
                    List<Map> subBuckets = (List<Map>) readMap(map, "http_status_nested.filter_status.status_groups.buckets");
                    Stream.of("2xx", "3xx", "4xx", "5xx").forEach(status ->
                            subBuckets.stream()
                                    .peek(subBucket -> log.trace(subBucket.toString()))
                                    .filter(subBucket -> subBucket.get("key").equals(status))
                                    .findFirst()
                                    .ifPresent(_2xx -> {
                                        log.trace(_2xx.toString());
                                        ret.put(status, readMap(_2xx, "doc_count"));
                                    }));
                    return ret;
                }).toList();
    }

    @SneakyThrows
    List<Map<String, Object>> overTimeByAppId(String gte) {
        log.trace("overTimeByAppId({})", gte);
        Map statusNested = (Map) runJson("/overTimeByAppId.json", Map.of("--gte--", gte)).get("over_time");
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

    private Map<String, Object> runJson(String json, @Nullable Map<String, Object> replacements) throws IOException {
        String entity = new String(this.getClass().getResourceAsStream(json).readAllBytes());
        if (replacements != null) {
            for (Map.Entry<String, Object> entry : replacements.entrySet()) {
                entity = entity.replace(entry.getKey(), entry.getValue().toString());
            }
        }

        Request request = new Request("POST", "/rcu-audit-data-michir/_search");
        request.setJsonEntity(entity);

        Response response = restClient.performRequest(request);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        InputStream content = response.getEntity().getContent();
        StreamUtils.copy(content, os);
        content.close();
        return (Map) new ObjectMapper().readValue(new String(os.toByteArray()), Map.class)
                .get("aggregations");
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
