package org.kockpit.kinesis.s3;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.kockpit.audit.stream.api.model.AuditReport;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RequiredArgsConstructor
@Slf4j
public class KinesisToS3Consumer implements AuditConsumer {

    private final S3Client s3Client;
    private final String bucketName;
    private final String keyPrefix;
    private final ObjectMapper objectMapper;

    @Override
    public void accept(AuditReport report) {
        String key = buildKey(report);
        try {
            byte[] json = objectMapper.writeValueAsBytes(report);
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType("application/json")
                            .build(),
                    RequestBody.fromBytes(json)
            );
            log.debug("Written record {} to s3://{}/{}", report.getId(), bucketName, key);
        } catch (Exception e) {
            log.error("Failed to write record {} to s3://{}: {}", report.getId(), bucketName, e.getMessage(), e);
        }
    }

    private String buildKey(AuditReport report) {
        StringBuilder sb = new StringBuilder();
        if (keyPrefix != null && !keyPrefix.isBlank()) {
            sb.append(keyPrefix).append("/");
        }
        if (report.getDomain() != null) sb.append(report.getDomain()).append("/");
        if (report.getEnv() != null) sb.append(report.getEnv()).append("/");
        if (report.getAppId() != null) sb.append(report.getAppId()).append("/");
        sb.append(report.getId()).append(".json");
        return sb.toString();
    }
}
