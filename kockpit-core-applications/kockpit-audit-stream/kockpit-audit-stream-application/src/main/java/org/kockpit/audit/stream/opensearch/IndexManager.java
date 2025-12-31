package org.kockpit.audit.stream.opensearch;

public interface IndexManager {
    
    String getWriteAlias(String domain, String env, int ttl);
}
