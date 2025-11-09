package org.kockpit.communication.storageaccount;

import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Consumer;
import org.kockpit.communication.Message;

import java.io.ByteArrayOutputStream;

@RequiredArgsConstructor
@Slf4j
public class StorageAccountConsumer implements Consumer {

    private final BlobContainerClient blobContainerClient;

    private final ObjectMapper objectMapper;

    @Override
    public Message poll(String domain, String env, String appId, String type) {
        String filePattern = "%s/%s/%s/%s".formatted(domain, env, appId, type);
        return blobContainerClient.listBlobs()
                .stream()
                .filter(blobItem -> blobItem.getName().startsWith(filePattern))
                .map(blobItem -> blobContainerClient.getBlobClient(blobItem.getName()))
                .map(blobClient -> {
                    log.trace("Reading blob {}", blobClient.getBlobName());
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    blobClient.downloadStream(os);
                    return readSource(os.toByteArray());
                })
                .min((o1, o2) -> Math.toIntExact(o1.getCreationDate() - o2.getCreationDate()))
                .stream()
                .findAny()
                .orElse(null);
    }

    @SneakyThrows
    Message readSource(byte[] input) {
        return objectMapper.readValue(input, Message.class);
    }
}
