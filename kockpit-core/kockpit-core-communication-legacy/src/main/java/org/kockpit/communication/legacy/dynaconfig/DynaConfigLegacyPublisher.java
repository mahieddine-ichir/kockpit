package org.kockpit.communication.legacy.dynaconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.kockpit.communication.Message;
import org.kockpit.communication.Publisher;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

@RequiredArgsConstructor
public class DynaConfigLegacyPublisher implements Publisher {

    private final S3Client s3Client;
    private final String bucketName;
    private final ObjectMapper objectMapper;

    private final DynaConfigLegacyMapper dynaConfigLegacyMapper = new DynaConfigLegacyMapper();

    @SneakyThrows
    @Override
    public void publish(Message message) {
        if (!message.getService().equals("dyna-config")) {
            return;
        }

        Optional.ofNullable(dynaConfigLegacyMapper.map(message))
                .ifPresent(dynaConfigLegacyMessage ->
                        write(dynaConfigLegacyMessage, message));
    }

    @SneakyThrows
    private void write(DynaConfigLegacyMessage dynaConfigLegacyMessage, Message message) {
        String path = formatFilename(
                message.getDomain(),
                message.getEnv(),
                message.getAppId()
        );
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            objectMapper.writeValue(os, dynaConfigLegacyMessage);
            byte[] jsonBytes = os.toByteArray();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key("%s/%s.json".formatted(path, message.getId()))
                    .contentType("application/json")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(jsonBytes));
        }
    }

    static String formatFilename(String domain, String env, String appId) {
        return "%s/%s/%s".formatted(domain, env, appId);
    }
}
