package com.accor.wcp.services.auditstream.notification.darkcanary;

import com.accor.wcp.services.auditstream.notification.AuditReportHelper;
import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.accor.wcp.services.auditstream.notification.AuditReportRequestConsumer;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.CanaryTestingSource;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.ConfigurationProvider;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryConfiguration;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryEndpoint;
import com.accor.wcp.services.auditstream.notification.darkcanary.fromaudit.FromAuditReportRequestProcessor;
import com.accor.wcp.services.auditstream.notification.darkcanary.fromremote.FromRemoteAuditReportRequestProcessor;
import com.accor.wcp.services.auditstream.notification.darkcanary.mappings.DarkCanaryMapper;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.*;
import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.OpenFeatureAPI;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.bulk.BulkRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditReportRequestConsumerForDarkCanary implements AuditReportRequestConsumer {

    private final ConfigurationProvider configurationProvider;

    private final FromAuditReportRequestProcessor fromAuditReportRequestProcessor;

    private final FromRemoteAuditReportRequestProcessor fromRemoteAuditReportRequestProcessor;

    private final DarkCanaryDocumentRepository darkCanaryDocumentRepository;

    private final DarkCanaryMapper darkCanaryMapper;

    private final OpenFeatureAPI openFeatureAPI;

    @Override
    public void accept(List<AuditReportRequest> auditReportRequests) {
        Client featureFlipping = openFeatureAPI.getClient();
        if (Boolean.FALSE.equals(featureFlipping.getBooleanValue("ff.darkcanary.enabled", false))) {
            return;
        }

        try {
            this.processInternal(auditReportRequests);
        } catch (Exception e) {
            log.error("Error processing data for Dark Canary", e);
        }
    }

    @SneakyThrows
    private void processInternal(List<AuditReportRequest> auditReportRequests) {
        if (CollectionUtils.isEmpty(auditReportRequests)) {
            return;
        }

        List<ConfiguredDarkCanaryIndexDocument> listStream = auditReportRequests.stream()
                .map(this::configure)
                .filter(this::hasDarkCanaryConfiguration)
                .collect(Collectors.groupingBy(this::byMode))
                .entrySet().stream()
                .map(this::processByMode)
                .flatMap(Collection::stream)
                .toList();

        // save to main index
        {
            BulkRequest request = listStream.stream()
                    .map(darkCanaryDocumentRepository::createIndexRequest)
                    .collect(BulkRequest::new, BulkRequest::add, (bulkRequest, bulkRequest2) ->
                            bulkRequest.add(bulkRequest2.requests()));

            darkCanaryDocumentRepository.save(request);
        }

        // save to secondary index (differences)
        {
            BulkRequest request = listStream.stream()
                    .map(this::createIndexRequestForDiff)
                    .flatMap(Collection::stream)
                    .map(darkCanaryDocumentRepository::createIndexRequestForDifferences)
                    .collect(BulkRequest::new, BulkRequest::add, (bulkRequest, bulkRequest2) ->
                            bulkRequest.add(bulkRequest2.requests()));

            darkCanaryDocumentRepository.save(request);
        }
    }

    private List<ConfiguredDarkCanaryIndexDocument> processByMode(Map.Entry<CanaryTestingSource, List<ConfiguredAuditReportRequest>> canaryTestingSourceListEntry) {
        return switch (canaryTestingSourceListEntry.getKey()) {
            case audit -> fromAuditReportRequestProcessor.process(canaryTestingSourceListEntry.getValue());
            case remote -> fromRemoteAuditReportRequestProcessor.process(canaryTestingSourceListEntry.getValue());
        };
    }

    private List<ConfiguredDarkCanaryIndexDocumentByDifference> createIndexRequestForDiff(ConfiguredDarkCanaryIndexDocument configuredDarkCanaryIndexDocument) {
        DarkCanaryIndexDocument darkCanaryIndexDocument = configuredDarkCanaryIndexDocument.darkCanaryIndexDocument();

        // fixme code duplication
        if (CollectionUtils.isEmpty(darkCanaryIndexDocument.getDifferences())) {
            return darkCanaryIndexDocument.getEndpoints().stream().map(endpoint -> {
                DarkCanaryIndexDocumentByDifference darkCanaryIndexDocumentByDifference = darkCanaryMapper.toDarkCanaryIndexDocumentByDifference(darkCanaryIndexDocument);
                darkCanaryIndexDocumentByDifference.setDifference(null);
                darkCanaryIndexDocumentByDifference.setEndpoint(endpoint);

                return new ConfiguredDarkCanaryIndexDocumentByDifference(darkCanaryIndexDocumentByDifference, configuredDarkCanaryIndexDocument.darkCanaryConfiguration());
            }).toList();
        } else {
            return darkCanaryIndexDocument.getDifferences().stream().flatMap(propertyDifference ->
                    darkCanaryIndexDocument.getEndpoints().stream().map(endpoint -> {
                        DarkCanaryIndexDocumentByDifference darkCanaryIndexDocumentByDifference = darkCanaryMapper.toDarkCanaryIndexDocumentByDifference(darkCanaryIndexDocument);
                        darkCanaryIndexDocumentByDifference.setDifference(propertyDifference);
                        darkCanaryIndexDocumentByDifference.setEndpoint(endpoint);

                        return new ConfiguredDarkCanaryIndexDocumentByDifference(darkCanaryIndexDocumentByDifference, configuredDarkCanaryIndexDocument.darkCanaryConfiguration());
                    })).toList();
        }
    }

    private CanaryTestingSource byMode(ConfiguredAuditReportRequest configuredAuditReportRequest) {
        return Optional.ofNullable(configuredAuditReportRequest.darkCanaryConfiguration())
                .map(DarkCanaryConfiguration::getTestingSource)
                .orElse(CanaryTestingSource.audit);
    }

    private ConfiguredAuditReportRequest configure(AuditReportRequest auditReportRequest) {
        return configurationProvider.getConfiguration(auditReportRequest)
                .map(darkCanaryConfiguration -> new ConfiguredAuditReportRequest(auditReportRequest, darkCanaryConfiguration))
                .orElse(new ConfiguredAuditReportRequest(auditReportRequest, null));
    }

    private boolean hasDarkCanaryConfiguration(ConfiguredAuditReportRequest configuredAuditReportRequest) {
        return Optional.ofNullable(configuredAuditReportRequest.darkCanaryConfiguration())
                .map(darkCanaryConfiguration -> hasEnv(darkCanaryConfiguration, configuredAuditReportRequest.auditReportRequest()) &&
                        hasUri(darkCanaryConfiguration, configuredAuditReportRequest.auditReportRequest())
                )
                .orElse(false);
    }

    private boolean hasEnv(DarkCanaryConfiguration darkCanaryConfiguration, AuditReportRequest auditReportRequest) {
        return darkCanaryConfiguration.getEndpoints().stream()
                .map(DarkCanaryEndpoint::getEnvironment)
                .anyMatch(env -> auditReportRequest.getEnv().equalsIgnoreCase(env));
    }

    private boolean hasUri(DarkCanaryConfiguration darkCanaryConfiguration, AuditReportRequest auditReportRequest) {
        return darkCanaryConfiguration.getEndpoints().stream()
                .map(DarkCanaryEndpoint::getUri)
                .anyMatch(uri ->
                        AuditReportHelper.readRequestUri(auditReportRequest).map(uri::equalsIgnoreCase).orElse(false)
                );
    }
}
