package com.accor.wcp.services.auditstream.notification.darkcanary.fromaudit;

import com.accor.wcp.services.auditstream.notification.darkcanary.JsonDiffComparator;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryConfiguration;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class DarkCanaryDocumentGroupByIdListAggregator implements Function<Map.Entry<List<KeyValuePair>, List<ConfiguredDarkCanaryIndexDocument>>, ConfiguredDarkCanaryIndexDocument> {

    private final JsonDiffComparator<String> jsonDiffComparator;

    /**
     * Reduce and Merge two identical (by ID) documents into one: (target, victim) -> target,
     * computing the difference.
     * @param listListEntry the function argument
     * @return a single merged document
     */
    @Override
    public ConfiguredDarkCanaryIndexDocument apply(Map.Entry<List<KeyValuePair>, List<ConfiguredDarkCanaryIndexDocument>> listListEntry) {
        if (listListEntry.getValue().size() == 1) {
            log.trace("Found 1 dark canary index document with id {}", listListEntry.getKey());
            return listListEntry.getValue().get(0);
        } else {
            // should be common to both
            DarkCanaryConfiguration darkCanaryConfiguration = listListEntry.getValue().get(0).darkCanaryConfiguration();

            DarkCanaryIndexDocument target = listListEntry.getValue().get(0).darkCanaryIndexDocument();
            DarkCanaryIndexDocument victim = listListEntry.getValue().get(1).darkCanaryIndexDocument();
            target.setResponseRight(victim.getResponseLeft());

            DiffDuration targetDuration = target.getDurations();
            if (targetDuration != null) {
                targetDuration.setRightDuration(victim.getDurations().getLeftDuration());
                if (targetDuration.getLeftDuration() != null && targetDuration.getRightDuration() != null) {
                    targetDuration.setDifference(targetDuration.getRightDuration() - targetDuration.getLeftDuration());
                }
            }

            try {
                target.setDifferences(computeDiff(target, darkCanaryConfiguration));
            } catch (Exception e) {
                log.error("Error computing diff for config {}", darkCanaryConfiguration, e);
                target.setErrorMessage(e.getMessage());
            }

            log.trace("Reduced {} into {}", victim, target);
            return new ConfiguredDarkCanaryIndexDocument(target, darkCanaryConfiguration);
        }
    }

    @SneakyThrows
    private List<PropertyDifference> computeDiff(DarkCanaryIndexDocument document, DarkCanaryConfiguration configuration) {
        return jsonDiffComparator.compare(document.getResponseLeft(), document.getResponseRight(), configuration);
    }
}
