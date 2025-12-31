package org.kockpit.audit.stream.opensearch;

import org.opensearch.common.settings.Settings;

public class OpensearchHelper {

    public static final Long INDEX_TEMPLATE_VERSION = 4L;
    public static final int POLICY_VERSION = 2;

    public static String getIndexPrefix(String domain, String indexSuffix, String env, int ttl) {
        return domain + "-" + indexSuffix + "-" + env + "-ttl" + ttl + "d";
    }

    public static String getAliasWrite(String domain, String indexSuffix, String env, int ttl) {
        return domain + "-" + indexSuffix + "-" + env + "-ttl" + ttl + "d-write";
    }

    public static String getAliasRead(String domain, String indexSuffix, String env) {
        return domain + "-" + indexSuffix + "-" + env + "-read";
    }

    public static Settings buildSettings(String aliasWrite) {
        return Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 0)
                // Alias to create when rollover (with "is_write_index": true)
                .put("opendistro.index_state_management.rollover_alias", aliasWrite)
                .build();
    }
}
