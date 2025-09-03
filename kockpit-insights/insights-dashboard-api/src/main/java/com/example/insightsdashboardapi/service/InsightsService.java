package com.example.insightsdashboardapi.service;

import com.example.insightsdashboardapi.dto.DashboardSummaryDto;
import com.example.insightsdashboardapi.dto.FiltersDto;
import com.example.insightsdashboardapi.dto.PieChartDataDto;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.RangeQueryBuilder;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InsightsService {

    private final RestHighLevelClient client;
    private final String indexName;

    @Autowired
    public InsightsService(RestHighLevelClient client,
                           @Value("${opensearch.insights_index:insightsss}") String indexName) {
        this.client = client;
        this.indexName = indexName;
    }


    private SearchSourceBuilder createSearchSourceBuilder(Instant from, Instant to) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (from != null || to != null) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("timestamp");
            if (from != null) {
                rangeQuery.gte(from.toEpochMilli());
            }
            if (to != null) {
                rangeQuery.lte(to.toEpochMilli());
            }
            boolQuery.must(rangeQuery);
        } else {
            boolQuery.must(QueryBuilders.matchAllQuery());
        }
        searchSourceBuilder.query(boolQuery);
        searchSourceBuilder.size(1000);
        searchSourceBuilder.sort("timestamp", SortOrder.DESC);

        return searchSourceBuilder;
    }

    private SearchResponse executeSearch(SearchSourceBuilder searchSourceBuilder) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(searchSourceBuilder);
        return client.search(searchRequest, RequestOptions.DEFAULT);
    }

    private boolean shouldIncludeDocument(Map<String, Object> stats, String domainFilter, String envFilter) {
        if (domainFilter == null && envFilter == null) {
            return true;
        }
        Map<String, Object> countsPerDomainEnv = (Map<String, Object>) stats.get("countsPerDomainEnv");
        if (countsPerDomainEnv == null || countsPerDomainEnv.isEmpty()) {
            return false;
        }
        for (String key : countsPerDomainEnv.keySet()) {
            String[] parts = key.split("\\|");
            if (parts.length == 2) {
                String documentDomain = parts[0];
                String documentEnv = parts[1];

                boolean domainMatches = domainFilter == null || domainFilter.equals(documentDomain);
                boolean envMatches = envFilter == null || envFilter.equals(documentEnv);

                if (domainMatches && envMatches) {
                    return true;
                }
            }
        }
        return false;
    }

    public DashboardSummaryDto getDashboardSummary(Instant from, Instant to, String domain, String env) {
        try {
            SearchSourceBuilder searchSourceBuilder = createSearchSourceBuilder(from, to);
            SearchResponse response = executeSearch(searchSourceBuilder);

            long totalActualRequests = 0;
            double totalSuccessWeighted = 0;
            double totalErrorWeighted = 0;
            double totalDurationWeighted = 0;
            int docCount = 0;

            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> source = hit.getSourceAsMap();
                Map<String, Object> stats = (Map<String, Object>) source.get("stats");

                if (stats != null && shouldIncludeDocument(stats, domain, env)) {
                    long batchRequests = getLongValue(stats, "totalRequests");
                    double batchSuccessRate = getDoubleValue(stats, "successRate");
                    double batchErrorRate = getDoubleValue(stats, "errorRate");
                    double batchAvgDuration = getDoubleValue(stats, "avgDuration");

                    totalActualRequests += batchRequests;
                    totalSuccessWeighted += (batchSuccessRate * batchRequests);
                    totalErrorWeighted += (batchErrorRate * batchRequests);
                    totalDurationWeighted += (batchAvgDuration * batchRequests);
                    docCount++;
                }
            }

            return DashboardSummaryDto.builder()
                    .totalRequests(totalActualRequests)
                    .averageSuccessRate(totalActualRequests > 0 ? totalSuccessWeighted / totalActualRequests : 0)
                    .averageErrorRate(totalActualRequests > 0 ? totalErrorWeighted / totalActualRequests : 0)
                    .averageDuration(totalActualRequests > 0 ? totalDurationWeighted / totalActualRequests : 0)
                    .build();

        } catch (IOException e) {
            log.error("error dashboard summary", e);
            return createEmptyDashboardSummary();
        }
    }

    public List<PieChartDataDto> getStatusDistribution(Instant from, Instant to, String domain, String env) {
        try {
            SearchSourceBuilder searchSourceBuilder = createSearchSourceBuilder(from, to);
            SearchResponse response = executeSearch(searchSourceBuilder);

            Map<String, Long> statusCounts = new HashMap<>();

            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> source = hit.getSourceAsMap();
                Map<String, Object> stats = (Map<String, Object>) source.get("stats");

                if (stats != null && shouldIncludeDocument(stats, domain, env)) {
                    Map<String, Object> statusDistribution = (Map<String, Object>) stats.get("statusDistribution");
                    if (statusDistribution != null) {
                        statusDistribution.forEach((key, value) -> {
                            Long count = value instanceof Number ? ((Number) value).longValue() : 0L;
                            statusCounts.merge("HTTP " + key, count, Long::sum);
                        });
                    }
                }
            }

            return statusCounts.entrySet().stream()
                    .map(entry -> PieChartDataDto.builder()
                            .label(entry.getKey())
                            .value(entry.getValue())
                            .build())
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Error status distribution", e);
            return new ArrayList<>();
        }
    }

    public List<PieChartDataDto> getMethodDistribution(Instant from, Instant to, String domain, String env) {
        try {
            SearchSourceBuilder searchSourceBuilder = createSearchSourceBuilder(from, to);
            SearchResponse response = executeSearch(searchSourceBuilder);

            Map<String, Long> methodCounts = new HashMap<>();

            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> source = hit.getSourceAsMap();
                Map<String, Object> stats = (Map<String, Object>) source.get("stats");

                if (stats != null && shouldIncludeDocument(stats, domain, env)) {
                    Map<String, Object> methodDistribution = (Map<String, Object>) stats.get("methodDistribution");
                    if (methodDistribution != null) {
                        methodDistribution.forEach((key, value) -> {
                            Long count = value instanceof Number ? ((Number) value).longValue() : 0L;
                            methodCounts.merge(key, count, Long::sum);
                        });
                    }
                }
            }

            return methodCounts.entrySet().stream()
                    .map(entry -> PieChartDataDto.builder()
                            .label(entry.getKey())
                            .value(entry.getValue())
                            .build())
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Error method distribution", e);
            return new ArrayList<>();
        }
    }

    public List<PieChartDataDto> getDomainEnvDistribution(Instant from, Instant to, String domain, String env) {
        try {
            SearchSourceBuilder searchSourceBuilder = createSearchSourceBuilder(from, to);
            SearchResponse response = executeSearch(searchSourceBuilder);

            Map<String, Long> domainEnvCounts = new HashMap<>();

            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> source = hit.getSourceAsMap();
                Map<String, Object> stats = (Map<String, Object>) source.get("stats");

                if (stats != null && shouldIncludeDocument(stats, domain, env)) {
                    Map<String, Object> countsPerDomainEnv = (Map<String, Object>) stats.get("countsPerDomainEnv");
                    if (countsPerDomainEnv != null) {
                        countsPerDomainEnv.forEach((key, value) -> {
                            Long count = value instanceof Number ? ((Number) value).longValue() : 0L;
                            String displayKey = key.replace("|", "/");
                            domainEnvCounts.merge(displayKey, count, Long::sum);
                        });
                    }
                }
            }

            return domainEnvCounts.entrySet().stream()
                    .map(entry -> PieChartDataDto.builder()
                            .label(entry.getKey())
                            .value(entry.getValue())
                            .build())
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Error domainenv distribution", e);
            return new ArrayList<>();
        }
    }



    private DashboardSummaryDto createEmptyDashboardSummary() {
        return DashboardSummaryDto.builder()
                .totalRequests(0L)
                .averageSuccessRate(0.0)
                .averageErrorRate(0.0)
                .averageDuration(0.0)
                .build();
    }


    public FiltersDto getAvailableFilters() {
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            searchSourceBuilder.size(1000);
            searchSourceBuilder.sort("timestamp", SortOrder.DESC);

            SearchRequest searchRequest = new SearchRequest(indexName);
            searchRequest.source(searchSourceBuilder);
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            Set<String> domains = new HashSet<>();
            Set<String> environments = new HashSet<>();

            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> source = hit.getSourceAsMap();
                Map<String, Object> stats = (Map<String, Object>) source.get("stats");

                if (stats != null) {
                    Map<String, Object> countsPerDomainEnv = (Map<String, Object>) stats.get("countsPerDomainEnv");
                    if (countsPerDomainEnv != null) {
                        countsPerDomainEnv.keySet().forEach(key -> {
                            String[] parts = key.split("\\|");
                            if (parts.length == 2) {
                                domains.add(parts[0]);
                                environments.add(parts[1]);
                            }
                        });
                    }
                }
            }

            return FiltersDto.builder()
                    .domains(new ArrayList<>(domains))
                    .environments(new ArrayList<>(environments))
                    .build();

        } catch (IOException e) {
            log.error("Error getting available filters", e);
            return FiltersDto.builder()
                    .domains(new ArrayList<>())
                    .environments(new ArrayList<>())
                    .build();
        }
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
    }
}