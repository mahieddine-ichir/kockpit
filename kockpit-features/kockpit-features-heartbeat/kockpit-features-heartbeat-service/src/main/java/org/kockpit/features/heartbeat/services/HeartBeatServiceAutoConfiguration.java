package org.kockpit.features.heartbeat.services;

import org.kockpit.core.sdk.ServiceDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class HeartBeatServiceAutoConfiguration {

    @Bean
    ServiceDefinition heartBeatServiceDefinition(
        @Value("${kockpit.sdk.heartbeat.polling.enabled:true}") boolean pollingEnabled
    ) {
        return new HeartBeatServiceDefinition(pollingEnabled);
    }
}
