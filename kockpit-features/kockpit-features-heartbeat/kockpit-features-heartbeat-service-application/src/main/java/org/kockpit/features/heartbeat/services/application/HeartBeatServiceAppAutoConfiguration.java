package org.kockpit.features.heartbeat.services.application;

import lombok.SneakyThrows;
import org.kockpit.communication.Publisher;
import org.kockpit.features.heartbeat.services.HeartBeatServiceDefinition;
import org.kockpit.sdk.SdkApplicationProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(SdkApplicationProperties.class)
@ConditionalOnProperty(
        value = "kockpit.heartbeat.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class HeartBeatServiceAppAutoConfiguration {

    @Bean
    InitializingBean heartBeatPublisher(
            List<Publisher> publishers,
            TaskScheduler taskScheduler,
            SdkApplicationProperties applicationProperties,
            HeartBeatServiceDefinition serviceDefinition,
            @Value("${kockpit.heartbeat.scheduling:PT1M}") String clientScheduling,
            @Value("${kockpit.hostname:#{null}}") String hostname
    ) {
        String instanceId = hostname;
        if (isNull(instanceId)) {
            instanceId = resolveHostname();
        }
        String startupId = UUID.randomUUID().toString();
        new HeartBeatPublisher(publishers, taskScheduler, serviceDefinition)
                .start(applicationProperties.getDomain(), applicationProperties.getEnv(), applicationProperties.getAppId(), instanceId, startupId, Duration.parse(clientScheduling));
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
