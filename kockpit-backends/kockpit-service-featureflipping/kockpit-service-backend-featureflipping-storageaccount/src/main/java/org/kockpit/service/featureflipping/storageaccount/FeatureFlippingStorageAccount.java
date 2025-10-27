package org.kockpit.service.featureflipping.storageaccount;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.service.featureflipping.api.FeatureFlippingDto;
import org.kockpit.service.featureflipping.api.FeatureFlippingHistory;
import org.kockpit.service.featureflipping.api.FeatureFlippingService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class FeatureFlippingStorageAccount implements FeatureFlippingService {

    private final BlobContainerClient blobContainerClient;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);

    @SneakyThrows
    @Override
    public FeatureFlippingDto update(String domain, String env, FeatureFlippingDto featureFlippingDto) {
        BlobClient blobClient = blobContainerClient.getBlobClient("%s/%s/%s.json".formatted(domain, env, featureFlippingDto.getKey()));
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            objectMapper.writeValue(os, featureFlippingDto);
            blobClient.upload(new ByteArrayInputStream(os.toByteArray()), os.size());
        }
        return featureFlippingDto;

    }

    @Override
    public List<FeatureFlippingHistory> getHistory(String domain, String env) {
        return List.of();
    }

    @Override
    public List<FeatureFlippingDto> findAll(String domain, String env) {
        return blobContainerClient.listBlobs()
                .stream()
                .filter(blobItem -> blobItem.getName().endsWith(".json"))
                .map(blobItem -> blobContainerClient.getBlobClient(blobItem.getName()))
                .map(blobClient -> {
                    log.trace("Reading blob {}", blobClient.getBlobName());
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    blobClient.downloadStream(os);
                    return readSource(os.toByteArray());
                })
                .toList();
    }

    @SneakyThrows
    FeatureFlippingDto readSource(byte[] input) {
        return objectMapper.readValue(input, FeatureFlippingDto.class);
    }
}
