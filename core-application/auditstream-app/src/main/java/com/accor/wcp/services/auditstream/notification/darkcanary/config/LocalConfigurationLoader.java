package com.accor.wcp.services.auditstream.notification.darkcanary.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
@Profile("local-config")
public class LocalConfigurationLoader implements ConfigurationLoader {

    @Value("${darkcanarytesting.configurations}")
    private String[] configurationsPaths;

    @Getter
    private List<DarkCanaryConfiguration> configurations;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    void init() {
        this.configurations = Stream.of(configurationsPaths)
                .map(String::trim)
                .map(this::read)
                .toList();
    }

    @SneakyThrows
    private DarkCanaryConfiguration read(String resourcePath) {
        return objectMapper.readValue(this.getClass().getResourceAsStream(resourcePath), DarkCanaryConfiguration.class);
    }
}
