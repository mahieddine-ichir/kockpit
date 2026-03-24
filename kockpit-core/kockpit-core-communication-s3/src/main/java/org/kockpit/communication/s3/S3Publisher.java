package org.kockpit.communication.s3;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.kockpit.communication.Message;
import org.kockpit.communication.Publisher;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

@RequiredArgsConstructor
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
                message.getType()
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
        }
    }


    @Override
    public void cleanup() {
        ListObjectsV2Iterable pages = s3Client.listObjectsV2Paginator(
                ListObjectsV2Request.builder().bucket(bucketName).build());
        pages.contents().stream()
                .filter(obj -> obj.key().endsWith(".json"))
                .forEach(obj -> s3Client.deleteObject(
                        DeleteObjectRequest.builder().bucket(bucketName).key(obj.key()).build()));
    }

    static String formatFilename(String domain, String env, String appId, String type) {
        if (Objects.isNull(appId)) {
            return "%s/%s/%s".formatted(domain, env, type);
        } else {
            return "%s/%s/%s/%s".formatted(domain, env, appId, type);
        }
    }
}
