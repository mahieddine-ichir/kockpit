package com.accor.wcp.services.auditstream.notification.darkcanary;

import com.accor.wcp.services.auditstream.notification.es.manager.opensearch.IndexCreationClient;
import com.accor.wcp.services.auditstream.notification.es.manager.opensearch.OpensearchHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DarkCanaryIndexManager {

    private final IndexCreationClient indexCreationClient;

    @Value("${opensearch.dark_canary.index_mapping_file}")
    private String indexMappingFile;

    @Value("${opensearch.dark_canary.index_suffix}")
    private String indexSuffix;

    String getAliasRead(String domain, String env, int ttl) {
        // force creation of indexes, if none
        indexCreationClient.getWriteAliasFor(domain, indexSuffix, env, ttl, indexMappingFile);

        return OpensearchHelper.getAliasRead(domain, indexSuffix, env);
    }

    String getWriteAliasFor(String domain, String env, int ttl) {
        return indexCreationClient.getWriteAliasFor(domain, indexSuffix, env, ttl, indexMappingFile);
    }

    String getDiffWriteAliasFor(String domain, String env, int ttl) {
        return indexCreationClient.getWriteAliasFor(domain, indexSuffix+"-diff", env, ttl, indexMappingFile);
    }
}
