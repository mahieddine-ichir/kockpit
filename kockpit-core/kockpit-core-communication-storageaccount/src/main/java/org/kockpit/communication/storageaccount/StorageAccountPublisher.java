package org.kockpit.communication.storageaccount;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.kockpit.communication.Message;
import org.kockpit.communication.Publisher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@RequiredArgsConstructor
public class StorageAccountPublisher implements Publisher {

    private final BlobContainerClient blobContainerClient;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public void publish(Message message) {
        BlobClient blobClient = blobContainerClient
                .getBlobClient(formatFilename(
                        message.getDomain(),
                        message.getEnv(),
                        message.getAppId(),
                        message.getType(),
                        message.getId(),
                        message.getCreationDate()
                ));
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            objectMapper.writeValue(os, message);
            blobClient.upload(new ByteArrayInputStream(os.toByteArray()), os.size());
        }
    }

    static String formatFilename(String domain, String env, String appId, String type, String id, long creationDate) {
        return "%s/%s/%s/%s/%s_%d.json".formatted(domain, env, appId, type, id, creationDate);
    }
}
