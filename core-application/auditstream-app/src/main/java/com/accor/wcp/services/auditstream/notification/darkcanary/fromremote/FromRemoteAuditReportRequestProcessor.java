package com.accor.wcp.services.auditstream.notification.darkcanary.fromremote;

import com.accor.wcp.services.auditstream.notification.AuditReportHelper;
import com.accor.wcp.services.auditstream.notification.darkcanary.AuditRequestProcessor;
import com.accor.wcp.services.auditstream.notification.darkcanary.JsonDiffComparator;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryConfiguration;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.TimeInterval;
import com.accor.wcp.services.auditstream.notification.darkcanary.mappings.AuditReportToDakCanaryDocumentMapper;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

import static com.accor.wcp.services.auditstream.notification.AuditReportHelper.readResponse;
import static org.springframework.util.StringUtils.hasText;

@Component
@RequiredArgsConstructor
@Slf4j
public class FromRemoteAuditReportRequestProcessor implements AuditRequestProcessor {

    private final AuditReportToDakCanaryDocumentMapper auditDataToDarkCanaryMapper;

    private final RemoteAuditRequestHttpCaller remoteAuditRequestHttpCaller;

    private final JsonDiffComparator<String> jsonComparator;

    private final DefaultTimeIntervalConfiguration defaultTimeIntervalConfiguration;

    @Value("${darkcanarytesting.defaults.call-rate}")
    private Double defaultCallRate;

    @Override
    public List<ConfiguredDarkCanaryIndexDocument> process(List<ConfiguredAuditReportRequest> auditReportRequests) {
        return auditReportRequests.stream()
                .filter(this::canExecute)
                .map(this::callForDarkCanaryTesting)
                .flatMap(Collection::stream)
                .toList();
    }

    private boolean canExecute(ConfiguredAuditReportRequest configuredAuditReportRequest) {
        List<TimeInterval> timeIntervals = Optional.of(configuredAuditReportRequest.darkCanaryConfiguration())
                .map(DarkCanaryConfiguration::getAllowedTimeIntervals)
                .filter(ts -> ts.length > 0)
                .map(Arrays::asList)
                .orElse(defaultTimeIntervalConfiguration.getRunTimeIntervals());

        LocalDateTime now = LocalDateTime.now();
        return timeIntervals.stream().anyMatch(timeInterval -> {
            try {
                return TimeUtils.isEligible(timeInterval, now);
            } catch (Exception e) {
                log.error("Error processing run time interval data {}", timeInterval, e);
                return false;
            }
        });
    }

    private List<ConfiguredDarkCanaryIndexDocument> callForDarkCanaryTesting(ConfiguredAuditReportRequest configuredAuditReportRequest) {

        // limit remote calls to configured rate, per configuration
        Double callRate = Optional.of(configuredAuditReportRequest.darkCanaryConfiguration())
                .map(DarkCanaryConfiguration::getCallRate)
                .orElse(defaultCallRate);

        if (Math.random() <= callRate) {
            return remoteAuditRequestHttpCaller.call(configuredAuditReportRequest)
                    .stream()
                    .map(remoteCallMetadata -> this.createDarkCanaryIndexDocument(remoteCallMetadata, configuredAuditReportRequest))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        } else {
            return Collections.emptyList();
        }
    }

    private Optional<ConfiguredDarkCanaryIndexDocument> createDarkCanaryIndexDocument(RemoteCallMetadata remoteCallMetadata, ConfiguredAuditReportRequest configuredAuditReportRequest) {
        return readResponse(configuredAuditReportRequest.auditReportRequest())
                .map(response -> this.createDarkCanaryIndexDocumentWithResponse(response, configuredAuditReportRequest, remoteCallMetadata));
    }

    private ConfiguredDarkCanaryIndexDocument createDarkCanaryIndexDocumentWithResponse(String responseBody, ConfiguredAuditReportRequest configuredAuditReportRequest, RemoteCallMetadata remoteCallMetadata) {
        ConfiguredDarkCanaryIndexDocument configuredDarkCanaryIndexDocument = this.auditDataToDarkCanaryMapper.apply(configuredAuditReportRequest);

        DarkCanaryIndexDocument darkCanaryIndexDocument = configuredDarkCanaryIndexDocument.darkCanaryIndexDocument();
        if (remoteCallMetadata.statusCode().is2xxSuccessful()) {
            List<PropertyDifference> propertyDifferences = jsonComparator.compare(responseBody, remoteCallMetadata.body(), configuredAuditReportRequest.darkCanaryConfiguration());
            darkCanaryIndexDocument.setDifferences(propertyDifferences);
        } else {
            darkCanaryIndexDocument.setDifferences(Collections.emptyList());
        }

        darkCanaryIndexDocument.setResponseRight(remoteCallMetadata.body());
        AuditReportHelper.readCallDuration(configuredAuditReportRequest.auditReportRequest())
                .map(aLong -> {
                    DiffDuration duration = new DiffDuration();
                    duration.setLeftDuration(aLong);
                    duration.setRightDuration(remoteCallMetadata.callDuration());
                    duration.setDifference(remoteCallMetadata.callDuration() - aLong);

                    return duration;
                }).ifPresent(darkCanaryIndexDocument::setDurations);

        // fixme use remoteDall endpoint reference
        darkCanaryIndexDocument.getEndpoints().stream()
                .filter(endpoint -> hasText(endpoint.getUri()))
                .findFirst()
                .ifPresent(endpoint -> this.enrichEndpoint(endpoint, remoteCallMetadata));

        return configuredDarkCanaryIndexDocument;
    }

    private void enrichEndpoint(Endpoint endpoint, RemoteCallMetadata remoteCallMetadata) {
        endpoint.setTargetUri(remoteCallMetadata.url());
        endpoint.setDuration(remoteCallMetadata.callDuration());
        endpoint.setMethod(remoteCallMetadata.method());
        endpoint.setStatusCode(remoteCallMetadata.statusCode().value());
        if (remoteCallMetadata.statusCode().isError()) {
            endpoint.setErrorMessage(remoteCallMetadata.errorMessage());
        }
    }
}
