package org.kockpit.audit.backend.azuresearch;

import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.kockpit.audit.backend.ConfigApiDelegate;
import org.kockpit.audit.backend.ConfigItem;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.util.List;

@RequiredArgsConstructor
public class StorageAccountFilesRepository implements ConfigApiDelegate {

    private final BlobContainerClient blobContainerClient;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

    @SneakyThrows
    @Override
    public ResponseEntity<List<ConfigItem>> getConfig() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (os) {
            blobContainerClient.getBlobClient("manifests/rcu-manifest.json")
                    .downloadStream(os);
            TypeReference<List<ConfigItem>> typeRef = new TypeReference<>() {};
            return ResponseEntity.ok(objectMapper.readValue(os.toByteArray(), typeRef));
        }
    }
}
