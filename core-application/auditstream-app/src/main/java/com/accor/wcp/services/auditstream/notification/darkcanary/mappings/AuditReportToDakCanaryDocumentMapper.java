package com.accor.wcp.services.auditstream.notification.darkcanary.mappings;

import com.accor.wcp.services.auditstream.notification.AuditReportHelper;
import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.ConfigurationProvider;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryConfiguration;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryIdConfiguration;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditReportToDakCanaryDocumentMapper implements Function<ConfiguredAuditReportRequest, ConfiguredDarkCanaryIndexDocument> {

    private final DarkCanaryMapper darkCanaryMapper;

    private final ConfigurationProvider configurationProvider;

    @Override
    public ConfiguredDarkCanaryIndexDocument apply(ConfiguredAuditReportRequest configuredAuditReportRequest) {
        AuditReportRequest auditReportRequest = configuredAuditReportRequest.auditReportRequest();

        DarkCanaryIndexDocument darkCanaryIndexDocument = darkCanaryMapper.toDarkCanaryIndexDocument(auditReportRequest);
        darkCanaryIndexDocument.setId(computeId(auditReportRequest));
        darkCanaryIndexDocument.setDurations(new DiffDuration());
        AuditReportHelper.readCallDuration(auditReportRequest)
                .ifPresent(aLong -> darkCanaryIndexDocument.getDurations().setLeftDuration(aLong));

        return new ConfiguredDarkCanaryIndexDocument(darkCanaryIndexDocument, configuredAuditReportRequest.darkCanaryConfiguration());
    }

    private List<KeyValuePair> computeId(AuditReportRequest auditReportRequest) {
        if (CollectionUtils.isEmpty(auditReportRequest.getIndexedKeyValues())) {
            log.trace("Indexed key values is empty");
            return null;
        }
        return configurationProvider.getConfiguration(auditReportRequest)
                .map(configuration -> this.extractIds(configuration, auditReportRequest))
                .orElse(Collections.emptyList());
    }

    private List<KeyValuePair> extractIds(DarkCanaryConfiguration darkCanaryConfiguration, AuditReportRequest auditReportRequest) {
        return darkCanaryConfiguration.getId().stream()
                .map(darkCanaryIdConfiguration -> this.extractId(darkCanaryIdConfiguration, auditReportRequest))
                .filter(Objects::nonNull)
                .toList();
    }

    private KeyValuePair extractId(DarkCanaryIdConfiguration darkCanaryIdConfiguration, AuditReportRequest auditReportRequest) {
        if (darkCanaryIdConfiguration.getType().equalsIgnoreCase("header")) {
            return AuditReportHelper.readHeaderValue(auditReportRequest, darkCanaryIdConfiguration.getKey())
                    .map(headerValue -> KeyValuePair.builder().key(darkCanaryIdConfiguration.getKey()).value(headerValue).build())
                    .orElseGet(() -> {
                        log.trace("No header value found for key {}", darkCanaryIdConfiguration.getKey());
                        return null;
                    });
        } else {
            log.error("ID Config type {} not supported", darkCanaryIdConfiguration.getType());
            return null;
        }
    }
}
