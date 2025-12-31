package org.kockpit.features.manifest.services.storageaccount;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.features.manifest.services.ManifestBackendRepository;
import org.kockpit.features.manifest.services.dto.ManifestDto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class StorageAccountRepository implements ManifestBackendRepository {

    private final BlobContainerClient blobContainerClient;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT);

    @SneakyThrows
    @Override
    public List<ManifestDto> findAll() {
        return blobContainerClient.listBlobs()
                .stream()
                .map(blobItem -> blobContainerClient.getBlobClient(blobItem.getName()))
                .map(blobClient -> {
                    log.trace("Reading blob {}", blobClient.getBlobName());
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    blobClient.downloadStream(os);

                    ManifestDto manifestDto = read(os.toByteArray());
                    manifestDto.setName(blobClient.getBlobName());
                    return manifestDto;
                })
                .toList();
    }

    @Override
    public Optional<ManifestDto> findByName(String name) {
        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(name);
            if (!blobClient.exists()) {
                return Optional.empty();
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            blobClient.downloadStream(os);

            ManifestDto manifestDto = read(os.toByteArray());
            manifestDto.setName(blobClient.getBlobName());
            return Optional.of(manifestDto);
        } catch (Exception e) {
            log.error("Error reading manifest from Storage Account", e);
            return Optional.empty();
        }
    }

    @SneakyThrows
    @Override
    public ManifestDto save(ManifestDto manifestDto) {
        String blobName = manifestDto.getName();
        if (blobName == null) {
            blobName = "%s-%s-%d.json".formatted(
                    manifestDto.getDomain(),
                    manifestDto.getEnv(),
                    Instant.now().getEpochSecond()
            );
        }

        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            objectMapper.writeValue(os, manifestDto);
            blobClient.upload(new ByteArrayInputStream(os.toByteArray()), os.size(), true);
        }

        manifestDto.setName(blobName);
        return manifestDto;
    }

    ManifestDto read(byte[] content) {
        try {
            TypeReference<ManifestDto> typeRef = new TypeReference<>() {};
            return objectMapper.readValue(content, typeRef);
        } catch (Exception e) {
            log.error("Error reading manifest from Storage Account", e);
            return new ManifestDto();
        }
    }
}