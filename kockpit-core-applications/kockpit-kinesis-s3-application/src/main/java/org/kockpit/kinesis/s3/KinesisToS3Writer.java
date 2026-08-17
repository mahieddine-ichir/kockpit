package org.kockpit.kinesis.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Message;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import tools.jackson.databind.ObjectMapper;

import java.util.function.Consumer;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
@Slf4j
public class KinesisToS3Writer implements Consumer<String> {

    private final S3Client s3Client;
    private final String bucketName;
    private final ObjectMapper objectMapper;
    private final KinesisRecordMapper mapper;

    @Override
    public void accept(String rawJson) {
        try {
            KinesisRecord record = objectMapper.readValue(rawJson, KinesisRecord.class);
            if (record.getServiceId() != null && record.getServiceId().equals("audit")) {
                log.debug(record.toString());
            }
            Message message = mapper.toMessage(record);
            String key = buildKey(record);
            writeToS3(key, message);
            log.trace("Written record to s3://{}/{}", bucketName, key);
        } catch (Exception e) {
            log.error("Failed to write record to s3://{}: {}", bucketName, e.getMessage(), e);
        }
    }

    private void writeToS3(String key, Message message) throws Exception {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType("application/json")
                        .build(),
                RequestBody.fromString(objectMapper.writeValueAsString(message))
        );
    }

    private String buildPrefix(KinesisRecord record) {
        String domain = nvl(record.getDomain(), "unknown");
        String env = nvl(record.getEnv(), "unknown");
        String appId = nvl(record.getApplicationId(), "unknown");
        String serviceId = mapper.resolveServiceId(record);
        if (isNull(serviceId)) {
            return domain + "/" + env + "/" + appId;
        } else {
            return domain + "/" + env + "/" + appId + "/" + serviceId;
        }
    }

    private String buildKey(KinesisRecord record) {
        return buildPrefix(record) + "/latest.json";
    }

    private String nvl(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }
}
