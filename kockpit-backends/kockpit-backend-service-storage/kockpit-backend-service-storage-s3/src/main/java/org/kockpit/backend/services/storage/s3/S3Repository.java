package org.kockpit.backend.services.storage.s3;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.backend.services.storage.ConfigApiService;
import org.kockpit.backend.services.storage.ConfigItem;
import org.kockpit.backend.services.storage.Manifest;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class S3Repository implements ConfigApiService {

    private final S3Client s3Client;
    private final String bucketName;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @SneakyThrows
    @Override
    public List<Manifest> list() {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        return s3Client.listObjectsV2(listRequest)
                .contents()
                .stream()
                .filter(s3Object -> s3Object.key().contains("manifest"))
                .map(s3Object -> {
                    log.trace("Reading S3 object {}", s3Object.key());

                    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Object.key())
                            .build();

                    ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);

                    List<ConfigItem> configs = read(objectBytes.asByteArray());
                    Manifest manifest = new Manifest();
                    manifest.setConfigs(configs);
                    manifest.setName(s3Object.key());
                    return manifest;
                })
                .toList();
    }

    @SneakyThrows
    @Override
    public ConfigItem save(ConfigItem configItem) {
        String key = "%s-%s-%d.json".formatted(
                configItem.getDomain(),
                configItem.getEnv(),
                Instant.now().getEpochSecond()
        );

        byte[] jsonBytes = objectMapper.writeValueAsBytes(configItem);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/json")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(jsonBytes));
        return configItem;
    }

    List<ConfigItem> read(byte[] content) {
        try {
            TypeReference<List<ConfigItem>> typeRef = new TypeReference<>() {};
            return objectMapper.readValue(content, typeRef);
        } catch (Exception e) {
            log.error("Error reading config from S3", e);
            return Collections.emptyList();
        }
    }
}