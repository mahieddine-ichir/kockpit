package org.kockpit.audit.backend.inmemory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.kockpit.audit.backend.ConfigApiDelegate;
import org.kockpit.audit.backend.ConfigItem;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RequiredArgsConstructor
public class InMemoryConfigRepository implements ConfigApiDelegate {

    private final ObjectMapper objectMapper;

    private List<ConfigItem> configItems;

    @SneakyThrows
    @PostConstruct
    void init() {
        TypeReference<List<ConfigItem>> typeReference = new TypeReference<>() {};
        configItems = objectMapper.readValue(this.getClass().getResourceAsStream("/config.json"), typeReference);
    }

    @Override
    public ResponseEntity<List<ConfigItem>> getConfig() {
        return ResponseEntity.ok(configItems);
    }
}
