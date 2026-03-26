package org.kockpit.communication.s3;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Consumer;
import org.kockpit.communication.Message;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.List;
import java.util.Objects;

import static org.kockpit.communication.s3.S3Publisher.formatFilename;

@RequiredArgsConstructor
@Slf4j
public class S3Consumer implements Consumer {

    private final S3Client s3Client;
    private final String bucketName;
    private final ObjectMapper objectMapper;

    @Override
    public List<Message> poll(String domain, String env, String appId, String type) {
        log.trace("Polling messages for domain {}, env {}, appId {}, type {}", domain, env, appId, type);
        String prefix = formatFilename(domain, env, appId, type);

        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        return s3Client.listObjectsV2(listRequest)
                .contents()
                .stream()
                .filter(obj -> !obj.key().endsWith("/"))
                .map(this::read)
                .filter(Objects::nonNull)
                .sorted((o1, o2) -> Math.toIntExact(o1.getCreationDate() - o2.getCreationDate()))
                .toList();
    }

    private Message read(S3Object s3Object) {
        try {
            log.info("Reading S3 object {}", s3Object.key());
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Object.key())
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
            return readSource(objectBytes.asByteArray());
        } catch (Exception e) {
            log.error("Error reading S3 object {}", s3Object.key(), e);
            return null;
        }
    }

    @SneakyThrows
    Message readSource(byte[] input) {
        if (input == null || input.length == 0) {
            return null;
        }
        return objectMapper.readValue(input, Message.class);
    }
}
