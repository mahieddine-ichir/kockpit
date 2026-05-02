package org.kockpit.communication.s3;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Message;
import org.kockpit.communication.Publisher;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
public class S3Publisher implements Publisher {

    private final S3Client s3Client;
    private final String bucketName;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public void publish(Message message) {
        String path = formatFilename(
                message.getDomain(),
                message.getEnv(),
                message.getAppId(),
                message.getService()
        );
        String key = "%s/%s.json".formatted(path, message.getId());
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            objectMapper.writeValue(os, message);
            byte[] jsonBytes = os.toByteArray();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("application/json")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(jsonBytes));
        } catch (Exception e) {
            log.error("Error creating S3 object: key {}, bucket {}", key, bucketName, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cleanup() {
        ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder().bucket(bucketName);
        s3Client.listObjectsV2Paginator(requestBuilder.build())
                .contents().stream()
                .filter(obj -> obj.key().endsWith(".json"))
                .forEach(obj -> s3Client.deleteObject(
                        DeleteObjectRequest.builder().bucket(bucketName).key(obj.key()).build()));
    }

    static String formatFilename(String domain, String env, String appId, String service) {
        if (Objects.isNull(appId)) {
            return "%s/%s/%s".formatted(domain, env, service);
        } else {
            return "%s/%s/%s/%s".formatted(domain, env, appId, service);
        }
    }
}
