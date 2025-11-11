package org.kockpit.communication.storageaccount;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Consumer;
import org.kockpit.communication.Message;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Objects;

import static org.kockpit.communication.storageaccount.StorageAccountPublisher.formatFilename;

@RequiredArgsConstructor
@Slf4j
public class StorageAccountConsumer implements Consumer {

    private final BlobContainerClient blobContainerClient;

    private final ObjectMapper objectMapper;

    @Override
    public List<Message> poll(String domain, String env, String appId, String type) {
        String filePattern = formatFilename(domain, env, appId, type);
        return blobContainerClient.listBlobs()
                .stream()
                .filter(blobItem -> blobItem.getName().startsWith(filePattern))
                .map(blobItem -> blobContainerClient.getBlobClient(blobItem.getName()))
                .map(this::read)
                .filter(Objects::nonNull)
                .sorted((o1, o2) -> Math.toIntExact(o1.getCreationDate() - o2.getCreationDate()))
                .toList();
    }

    private Message read(BlobClient blobClient) {
        try {
            log.trace("Reading blob {}", blobClient.getBlobName());
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            blobClient.downloadStream(os);
            return readSource(os.toByteArray());
        } catch (Exception e) {
            log.error("Error reading blob {}", blobClient.getBlobName(), e);
            return null;
        }
    }

    @SneakyThrows
    Message readSource(byte[] input) {
        return objectMapper.readValue(input, Message.class);
    }
}
