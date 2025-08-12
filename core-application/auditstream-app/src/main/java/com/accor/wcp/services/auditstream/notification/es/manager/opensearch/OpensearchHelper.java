package com.accor.wcp.services.auditstream.notification.es.manager.opensearch;

import lombok.experimental.UtilityClass;
import org.opensearch.common.settings.Settings;

@UtilityClass
public class OpensearchHelper {

    public static final Long INDEX_TEMPLATE_VERSION = 4L;
    public static final int POLICY_VERSION = 2;

    public String getIndexPrefix(String domain, String indexSuffix, String env, int ttl) {
        return domain + "-" + indexSuffix + "-" + env + "-ttl" + ttl + "d";
    }

    public String getAliasWrite(String domain, String indexSuffix, String env, int ttl) {
        return domain + "-" + indexSuffix + "-" + env + "-ttl" + ttl + "d-write";
    }

    public String getAliasRead(String domain, String indexSuffix, String env) {
        return domain + "-" + indexSuffix + "-" + env + "-read";
    }

    public Settings buildSettings(String aliasWrite) {
        return Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 0)
                // Alias to create when rollover (with "is_write_index": true)
                .put("opendistro.index_state_management.rollover_alias", aliasWrite)
                .build();
    }
}
