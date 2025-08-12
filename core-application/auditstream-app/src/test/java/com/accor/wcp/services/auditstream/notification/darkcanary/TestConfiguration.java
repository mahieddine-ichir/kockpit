package com.accor.wcp.services.auditstream.notification.darkcanary;

import com.accor.wcp.services.auditstream.notification.darkcanary.config.ConfigurationLoader;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryConfiguration;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryIdConfiguration;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryWatch;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
class TestConfiguration {

    @Bean
    ConfigurationLoader configurationLoader() {
        return () -> List.of(
                DarkCanaryConfiguration.builder()
                        .domain("wcplatform")
                        .appId("wcpsamples")
                        .id(List.of(
                                DarkCanaryIdConfiguration.builder()
                                        .key("X-B3-TraceId")
                                        .type("header")
                                        .build()
                        ))
                        .includes(List.of(
                                DarkCanaryWatch.builder().left("$.firstName").right("$.firstName").build(),
                                DarkCanaryWatch.builder().left("$.phoneNumber.number").build()
                        )).build()
        );
    }
}