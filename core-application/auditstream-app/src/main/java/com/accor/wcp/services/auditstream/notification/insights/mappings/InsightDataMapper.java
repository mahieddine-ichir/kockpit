package com.accor.wcp.services.auditstream.notification.insights.mappings;

import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.accor.wcp.services.auditstream.notification.insights.model.InsightIndexRequest;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Component
@Slf4j
public class InsightDataMapper {

    @Value("${opensearch.insights.insight_data}")
    private String[] insightData;

    private Set<String> insightDataAsCollection;

    @PostConstruct
    void onInit() {
        this.insightDataAsCollection = new HashSet<>(Arrays.asList(insightData));
    }

    public List<InsightIndexRequest> toInsight(List<AuditReportRequest> auditReportRequests) {
        return auditReportRequests.stream()
                .map(this::toInsight)
                .toList();
    }

    InsightIndexRequest toInsight(AuditReportRequest auditReportRequest) {
        // sources data
        InsightIndexRequest insightIndexRequest = InsightIndexRequest.builder()
                .docId(auditReportRequest.getRequestId())
                .domain(auditReportRequest.getDomain())
                .env(auditReportRequest.getEnv())
                .ttl(auditReportRequest.getTtl())
                .build();

        insightIndexRequest.add("env", auditReportRequest.getEnv())
                .add("start", auditReportRequest.getStart().toEpochMilli())
                .add("end", auditReportRequest.getEnd().toEpochMilli())
                .add("version", auditReportRequest.getVersion())
                .add("ttl", auditReportRequest.getTtl())
                .add("appId", auditReportRequest.getAppId())
                .add("domain", auditReportRequest.getDomain());

        this.extractKeyValues(auditReportRequest)
                .forEach(stringObjectSimpleEntry -> insightIndexRequest.add(stringObjectSimpleEntry.getKey(), stringObjectSimpleEntry.getValue()));

        return insightIndexRequest;
    }

    private Collection<AbstractMap.SimpleEntry<String, Object>> extractKeyValues(AuditReportRequest auditReportRequest) {
        if (CollectionUtils.isEmpty(auditReportRequest.getIndexedKeyValues())) {
            return Collections.emptyList();
        }
        return auditReportRequest.getIndexedKeyValues().stream()
                .filter(map -> insightDataAsCollection.contains(map.get("key").toString()))
                .map(map -> new AbstractMap.SimpleEntry<>(map.get("key").toString(), map.get("value")))
                .toList();
    }
}
