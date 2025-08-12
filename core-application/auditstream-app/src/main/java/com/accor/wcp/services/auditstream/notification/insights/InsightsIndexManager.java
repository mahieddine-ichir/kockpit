package com.accor.wcp.services.auditstream.notification.insights;

import com.accor.wcp.services.auditstream.notification.es.manager.opensearch.IndexCreationClient;
import com.accor.wcp.services.auditstream.notification.es.manager.opensearch.OpensearchHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InsightsIndexManager {

    private final IndexCreationClient indexCreationClient;

    @Value("${opensearch.insights.index_mapping_file}")
    private String indexMappingFile;

    @Value("${opensearch.insights.index_suffix}")
    private String indexSuffix;

    String getAliasRead(String domain, String env) {
        return OpensearchHelper.getAliasRead(domain, indexSuffix, env);
    }

    String getWriteAliasFor(String domain, String env, int ttl) {
        return indexCreationClient.getWriteAliasFor(domain, indexSuffix, env, ttl, indexMappingFile);
    }
}
