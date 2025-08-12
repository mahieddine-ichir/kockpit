package com.accor.wcp.services.auditstream.notification.darkcanary.fromaudit;

import com.accor.wcp.services.auditstream.notification.darkcanary.DarkCanaryDocumentRepository;
import com.accor.wcp.services.auditstream.notification.darkcanary.JsonDiffComparator;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryConfiguration;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.ConfiguredDarkCanaryIndexDocument;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.DarkCanaryIndexDocument;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.DiffDuration;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.PropertyDifference;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Merges/Aggregates a DarkCanary Document with an eventually existing document
 * in the repository (namely OS): sets the first's response as the second response
 * of the already existing document.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DarkCanaryDocumentRepositoryAggregator implements Function<ConfiguredDarkCanaryIndexDocument, ConfiguredDarkCanaryIndexDocument> {

    private final JsonDiffComparator<String> jsonDiffComparator;

    private final DarkCanaryDocumentRepository darkCanaryDocumentRepository;

    @Override
    public ConfiguredDarkCanaryIndexDocument apply(ConfiguredDarkCanaryIndexDocument configuredDarkCanaryIndexDocument) {
        DarkCanaryIndexDocument darkCanaryIndexDocument = configuredDarkCanaryIndexDocument.darkCanaryIndexDocument();
        DarkCanaryConfiguration darkCanaryConfiguration = configuredDarkCanaryIndexDocument.darkCanaryConfiguration();

        return this.darkCanaryDocumentRepository.find(darkCanaryIndexDocument, darkCanaryConfiguration)
                .map(existing -> {
                    log.trace("Found an existing DarkCanaryIndexDocument with id {}", existing.getId());
                    existing.setResponseRight(darkCanaryIndexDocument.getResponseLeft());
                    // merge endpoints
                    existing.setEndpoints(List.of(existing.getEndpoints().get(0), darkCanaryIndexDocument.getEndpoints().get(0)));
                    existing.setDifferences(computeDiff(existing, darkCanaryConfiguration));

                    DiffDuration duration = existing.getDurations();
                    if (duration != null) {
                        duration.setRightDuration(darkCanaryIndexDocument.getDurations().getLeftDuration());
                        duration.setDifference(duration.getRightDuration() - duration.getLeftDuration());
                    }

                    // set temp id & mark as aggregated
                    existing.setAggregated(true);
                    existing.set_id(existing.get_id());

                    return new ConfiguredDarkCanaryIndexDocument(existing, darkCanaryConfiguration);
                }).orElse(configuredDarkCanaryIndexDocument);
    }

    @SneakyThrows
    private List<PropertyDifference> computeDiff(DarkCanaryIndexDocument document, DarkCanaryConfiguration configuration) {
        try {
            return jsonDiffComparator.compare(document.getResponseLeft(), document.getResponseRight(), configuration);
        } catch (Exception e) {
            log.error("Error comparing/reading json's", e);
            return Collections.emptyList();
        }
    }
}
