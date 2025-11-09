package org.kockpit.features.heartbeat.services.application;

import lombok.SneakyThrows;
import org.kockpit.communication.Publisher;
import org.kockpit.features.heartbeat.services.HeartBeatServiceDefinition;
import org.kockpit.sdk.SdkApplicationProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.time.Duration;

import static java.util.Objects.isNull;

@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(SdkApplicationProperties.class)
public class HeartBeatServiceAppAutoConfiguration {

//    @ConditionalOnBean({Publisher.class, HeartBeatServiceDefinition.class})
    @ConditionalOnProperty(value = "kockpit.sdk.heartbeat.enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    InitializingBean heartBeatPublisher(
            Publisher publisher,
            TaskScheduler taskScheduler,
            SdkApplicationProperties applicationProperties,
            HeartBeatServiceDefinition serviceDefinition,
            @Value("${kockpit.sdk.heartbeat.scheduling:PT10S}") String clientScheduling,
            @Value("${kockpit.sdk.hostname:#{null}}") String hostname
    ) {
        String instanceId = hostname;
        if (isNull(instanceId)) {
            instanceId = resolveHostname();
        }
        new HeartBeatPublisher(publisher, taskScheduler, serviceDefinition)
                .start(applicationProperties.getDomain(), applicationProperties.getEnv(), applicationProperties.getAppId(), instanceId, Duration.parse(clientScheduling));
        return () -> {};
    }

    @SneakyThrows
    private String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "UNKNOWN-"+Math.random();
        }
    }
}
