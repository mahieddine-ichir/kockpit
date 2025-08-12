package com.accor.wcp.services.auditstream.notification.darkcanary.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * fixme authenticate call to remote service endpoint?
 */
@Component
@Slf4j
@ConditionalOnProperty(value = "darkcanarytesting.console-services.manifest.enabled", havingValue = "true")
public class ConsoleServicesConfigurationLoader implements ConfigurationLoader {

    @Value("${darkcanarytesting.console-services.manifest.url}")
    private String manifestUrl;

    @Getter
    private List<DarkCanaryConfiguration> configurations = new ArrayList<>();

    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(fixedRateString = "${darkcanarytesting.console-services.manifest.refresh_rate_ms:10_000}")
    void refresh() {
        try {
            DarkCanaryConfiguration[] forObject = restTemplate.getForObject(manifestUrl, DarkCanaryConfiguration[].class);
            log.trace("Refreshing Dark Canary Configurations {}", forObject);
            if (forObject != null) {
                this.configurations = Arrays.asList(forObject);
            }
        } catch (Exception e) {
            log.error("Failed to refresh Dark Canary Configurations", e);
        }
    }
}
