package com.accor.wcp.services.auditstream.notification.darkcanary.config;

import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigurationProvider {

    private final ConfigurationLoader configurationLoader;

    public Optional<DarkCanaryConfiguration> getConfiguration(AuditReportRequest auditReportRequest) {
        return getConfiguration(auditReportRequest.getAppId(), auditReportRequest.getDomain());
    }

    public Optional<DarkCanaryConfiguration> getConfiguration(String appId, String domain) {
        return this.configurationLoader.getConfigurations().stream()
                .filter(darkCanaryConfiguration ->
                        darkCanaryConfiguration.getAppId().equalsIgnoreCase(appId) && darkCanaryConfiguration.getDomain().equalsIgnoreCase(domain))
                .findFirst();
    }
}
