package org.kockpit.features.manifest.services.filesystem;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.features.manifest.services.ManifestBackendRepository;
import org.kockpit.features.manifest.services.ManifestJson;
import org.kockpit.features.manifest.services.dto.ManifestDto;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class FilesystemRepository implements ManifestBackendRepository {

    private final String localFilePath;

    private final ObjectMapper objectMapper = ManifestJson.mapper();

    @SneakyThrows
    @Override
    public List<ManifestDto> findAll() {
        log.trace("Loading manifests from {}", localFilePath);

        Path dirPath = Path.of(localFilePath);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
            return List.of();
        }

        return Files.list(dirPath)
                .filter(path -> !path.toFile().isDirectory())
                .filter(path -> path.toString().endsWith(".json"))
                .map(this::read)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Optional<ManifestDto> findByName(String name) {
        try {
            Path filePath = Path.of(localFilePath, name);
            if (!Files.exists(filePath)) {
                return Optional.empty();
            }

            ManifestDto manifestDto = read(filePath);
            return Optional.ofNullable(manifestDto);
        } catch (Exception e) {
            log.error("Error reading manifest from filesystem", e);
            return Optional.empty();
        }
    }

    @SneakyThrows
    @Override
    public ManifestDto save(ManifestDto manifestDto) {
        String fileName = manifestDto.getName();
        if (fileName == null) {
            fileName = "%s-%s-%d.json".formatted(
                    manifestDto.getDomain(),
                    manifestDto.getEnv(),
                    Instant.now().getEpochSecond()
            );
        }

        Path dirPath = Path.of(localFilePath);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        File file = new File(localFilePath, fileName);
        objectMapper.writeValue(file, manifestDto);

        manifestDto.setName(fileName);
        return manifestDto;
    }

    private ManifestDto read(Path path) {
        try {
            TypeReference<ManifestDto> typeRef = new TypeReference<>() {};
            ManifestDto manifestDto = objectMapper.readValue(new FileInputStream(path.toFile()), typeRef);
            manifestDto.setName(path.toFile().getName());
            return manifestDto;
        } catch (Exception e) {
            log.error("Error reading manifest from {}", path, e);
            return null;
        }
    }
}