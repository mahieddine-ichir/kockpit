package org.kockpit.features.heartbeat.services;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class HeartBeatServiceAutoConfiguration {

    @Bean
    HeartBeatServiceDefinition heartBeatServiceDefinition() {
        return new HeartBeatServiceDefinition();
    }
}
