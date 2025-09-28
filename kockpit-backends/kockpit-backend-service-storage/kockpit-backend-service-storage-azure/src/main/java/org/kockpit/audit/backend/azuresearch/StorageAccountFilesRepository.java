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

import java.io.ByteArrayOutputStream;
import java.util.Collection;
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
    public List<ConfigItem> getConfig() {
        return blobContainerClient.listBlobs()
                .stream()
                .filter(blobItem -> blobItem.getName().endsWith(".json"))
                .map(blobItem -> blobContainerClient.getBlobClient(blobItem.getName()))
                .map(blobClient -> {
                    log.debug("Reading blob {}", blobClient.getBlobName());
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    blobClient.downloadStream(os);

                    return read(os);
                })
                .flatMap(Collection::stream)
                .toList();
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
