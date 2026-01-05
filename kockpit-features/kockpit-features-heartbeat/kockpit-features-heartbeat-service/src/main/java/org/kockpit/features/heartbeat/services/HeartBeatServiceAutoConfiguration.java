package org.kockpit.features.heartbeat.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class HeartBeatServiceAutoConfiguration {

    @Bean
    HeartBeatServiceDefinition heartBeatServiceDefinition(
        @Value("${kockpit.heartbeat.polling.enabled:true}") boolean pollingEnabled
    ) {
        return new HeartBeatServiceDefinition(pollingEnabled);
    }
}
