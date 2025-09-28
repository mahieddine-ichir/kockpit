package org.kockpit.backend.storage.local;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.backend.ConfigApiDelegate;
import org.kockpit.audit.backend.ConfigItem;
import org.springframework.http.ResponseEntity;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class LocalFilesRepository implements ConfigApiDelegate {

    private final String localFilePath;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

    @SneakyThrows
    @Override
    public ResponseEntity<List<ConfigItem>> getConfig() {
        List<ConfigItem> list = Files.list(Path.of(localFilePath))
                .map(this::read)
                .flatMap(Collection::stream)
                .toList();

        return ResponseEntity.ok(list);
    }

    List<ConfigItem> read(Path path) {
        try {
            TypeReference<List<ConfigItem>> typeRef = new TypeReference<>() {};
            return objectMapper.readValue(new FileInputStream(path.toFile()), typeRef);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
