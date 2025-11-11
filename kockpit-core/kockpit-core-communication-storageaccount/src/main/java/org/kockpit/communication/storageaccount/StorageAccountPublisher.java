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
import java.util.Objects;

@RequiredArgsConstructor
public class StorageAccountPublisher implements Publisher {

    private final BlobContainerClient blobContainerClient;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public void publish(Message message) {
        String blobName = formatFilename(
                message.getDomain(),
                message.getEnv(),
                message.getAppId(),
                message.getType()
        );
        String fileName = "%s/%s.json".formatted(blobName, message.getId());
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            objectMapper.writeValue(os, message);
            blobClient.upload(new ByteArrayInputStream(os.toByteArray()), os.size(), true);
        }
    }

    static String formatFilename(String domain, String env, String appId, String type) {
        if (Objects.isNull(appId)) {
            return "%s/%s/%s".formatted(domain, env, type);
        } else {
            return "%s/%s/%s/%s".formatted(domain, env, appId, type);
        }
    }
}
