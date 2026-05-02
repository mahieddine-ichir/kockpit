package org.kockpit.features.cache.service;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CacheServiceAutoConfiguration {

    @Bean
    CacheServiceDefinition cacheServiceDefinition() {
        return new CacheServiceDefinition();
    }
}