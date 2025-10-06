package org.kockpit.audit.backend.azuresearch;

import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.backend.services.storage.ConfigApiService;
import org.kockpit.backend.services.storage.ConfigItem;
import org.kockpit.backend.services.storage.Manifest;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class StorageAccountFilesRepository implements ConfigApiService {

    private final BlobContainerClient blobContainerClient;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

    @SneakyThrows
    @Override
    public List<Manifest> getConfig() {
        return blobContainerClient.listBlobs()
                .stream()
                .filter(blobItem -> blobItem.getName().endsWith(".json"))
                .map(blobItem -> blobContainerClient.getBlobClient(blobItem.getName()))
                .map(blobClient -> {
                    log.debug("Reading blob {}", blobClient.getBlobName());
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    blobClient.downloadStream(os);

                    List<ConfigItem> read = read(os);
                    Manifest manifest = new Manifest();
                    manifest.setConfigs(read);
                    manifest.setName(blobClient.getBlobName());
                    return manifest;
                })
                .toList();
    }

    @Override
    public ConfigItem save(ConfigItem configItem) {
        throw new RuntimeException("not implemented");
    }

    List<ConfigItem> read(ByteArrayOutputStream os) {
        try {
            TypeReference<List<ConfigItem>> typeRef = new TypeReference<>() {};
            return objectMapper.readValue(os.toByteArray(), typeRef);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
