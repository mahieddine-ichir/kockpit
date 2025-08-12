package com.accor.wcp.services.auditstream.notification.darkcanary.fromaudit;

import com.accor.wcp.services.auditstream.notification.darkcanary.AuditRequestProcessor;
import com.accor.wcp.services.auditstream.notification.darkcanary.mappings.AuditReportToDakCanaryDocumentMapper;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.ConfiguredAuditReportRequest;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.ConfiguredDarkCanaryIndexDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

@Component
@RequiredArgsConstructor
@Slf4j
public class FromAuditReportRequestProcessor implements AuditRequestProcessor {

    private final AuditReportToDakCanaryDocumentMapper auditDataToDarkCanaryMapper;

    private final DarkCanaryDocumentGroupByIdListAggregator darkCanaryDocumentGroupByIdListAggregator;

    private final DarkCanaryDocumentRepositoryAggregator darkCanaryDocumentRepositoryAggregator;

    @Override
    public List<ConfiguredDarkCanaryIndexDocument> process(List<ConfiguredAuditReportRequest> auditReportRequests) {
        return auditReportRequests.stream()
                .map(auditDataToDarkCanaryMapper)
                .filter(this::hasId)
                // group/collect within the list, those with the same ID
                .collect(Collectors.groupingBy(configuredDarkCanaryIndexDocument -> configuredDarkCanaryIndexDocument.darkCanaryIndexDocument().getId()))
                // group & merge those with the same ID (if two => the second will hold the v2 response)
                .entrySet().stream().map(darkCanaryDocumentGroupByIdListAggregator) // reduce those having same ID to a single request
                // aggregate with any already existing document in the ES.index
                .map(darkCanaryDocumentRepositoryAggregator)
                .toList();
    }

    private boolean hasId(ConfiguredDarkCanaryIndexDocument configuredDarkCanaryIndexDocument) {
        return configuredDarkCanaryIndexDocument.darkCanaryIndexDocument().getId().stream()
                .allMatch(keyValuePair -> hasText(keyValuePair.getKey()) && hasText(keyValuePair.getValue()));
    }
}
