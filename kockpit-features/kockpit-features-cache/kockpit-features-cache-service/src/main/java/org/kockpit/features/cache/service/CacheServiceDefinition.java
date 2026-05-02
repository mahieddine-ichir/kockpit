package org.kockpit.features.cache.service;

import org.kockpit.core.sdk.ServiceDefinition;

public class CacheServiceDefinition implements ServiceDefinition {

    public static final String CACHE = "cache";

    @Override
    public String name() {
        return CACHE;
    }
}