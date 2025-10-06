package org.kockpit.backend.services.storage.filesystem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.backend.services.storage.ConfigApiService;
import org.kockpit.backend.services.storage.ConfigItem;
import org.kockpit.backend.services.storage.Manifest;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class FileSystemRepository implements ConfigApiService {

    private final String localFilePath;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

    @SneakyThrows
    @Override
    public List<Manifest> getConfig() {
        log.debug("loading config from {}", localFilePath);
        return Files.list(Path.of(localFilePath))
                .map(this::read)
                .filter(Objects::nonNull)
                .toList();
    }

    @SneakyThrows
    @Override
    public ConfigItem save(ConfigItem configItem) {
        objectMapper.writeValue(new File(localFilePath, UUID.randomUUID().toString()), configItem);
        return configItem;
    }

    private Manifest read(Path path) {
        try {
            TypeReference<List<ConfigItem>> typeRef = new TypeReference<>() {};
            List<ConfigItem> configItems = objectMapper.readValue(new FileInputStream(path.toFile()), typeRef);
            Manifest manifest = new Manifest();
            manifest.setConfigs(configItems);
            manifest.setName(path.toFile().getName());
            return manifest;
        } catch (Exception e) {
            log.error("error reading config from {}", localFilePath, e);
            return null;
        }
    }
}
