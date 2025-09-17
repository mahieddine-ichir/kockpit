package com.example.kockpitbackendserviceopensearch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.kockpit.audit.backend.ConfigApiDelegate;
import org.kockpit.audit.backend.ConfigItem;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class LocalFileConfigRepository implements ConfigApiDelegate {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @Override
    public ResponseEntity<List<ConfigItem>> getConfig(String domain, String appId) {
        try (InputStream is = getClass().getResourceAsStream("/config.json")) {
            if (is == null) {
                return ResponseEntity.notFound().build();
            }
            TypeReference<List<ConfigItem>> typeRef = new TypeReference<>() {};
            List<ConfigItem> allConfigs = objectMapper.readValue(is, typeRef);
            List<ConfigItem> filteredConfigs = allConfigs.stream()
                    .filter(config -> domain.equals(config.getDomain()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(filteredConfigs);
        }
    }
}