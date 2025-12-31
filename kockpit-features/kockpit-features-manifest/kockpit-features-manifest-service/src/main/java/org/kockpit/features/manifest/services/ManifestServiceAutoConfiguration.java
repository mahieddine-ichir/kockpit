package org.kockpit.features.manifest.services;

import org.kockpit.core.sdk.ServiceDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ManifestServiceAutoConfiguration {

    @Bean
    ServiceDefinition manifestServiceDefinition(
        @Value("${kockpit.sdk.manifest.enabled:true}") boolean enabled
    ) {
        return new ManifestServiceDefinition(enabled);
    }
}