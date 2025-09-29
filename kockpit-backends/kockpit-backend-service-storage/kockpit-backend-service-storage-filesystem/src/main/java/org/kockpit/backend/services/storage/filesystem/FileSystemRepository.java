package org.kockpit.backend.services.storage.filesystem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.backend.services.storage.ConfigApiService;
import org.kockpit.backend.services.storage.ConfigItem;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    public List<ConfigItem> getConfig() {
        log.debug("loading config from {}", localFilePath);
        List<ConfigItem> list = Files.list(Path.of(localFilePath))
                .map(this::read)
                .flatMap(Collection::stream)
                .toList();

        return list;
    }

    @SneakyThrows
    @Override
    public ConfigItem save(ConfigItem configItem) {
        objectMapper.writeValue(new File(localFilePath, UUID.randomUUID().toString()), configItem);
        return configItem;
    }

    private List<ConfigItem> read(Path path) {
        try {
            TypeReference<List<ConfigItem>> typeRef = new TypeReference<>() {};
            return objectMapper.readValue(new FileInputStream(path.toFile()), typeRef);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
