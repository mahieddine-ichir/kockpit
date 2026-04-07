package org.kockpit.communication.legacy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.kockpit.communication.Message;
import org.kockpit.communication.Publisher;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class DynaConfigLegacyPublisher implements Publisher {

    private final S3Client s3Client;
    private final String bucketName;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public void publish(Message message) {
        if (! message.getType().equals("dyna-config")) {
            return;
        }

        List<PropertyUpdateMessageRequestDto> propertyUpdateMessageRequestDtos = message.getKeyValues().stream()
                .map(keyValue -> PropertyUpdateMessageRequestDto.builder()
                        .propertyName(keyValue.key())
                        .newValue(keyValue.value())
                        .build())
                .toList();

        DynaConfigLegacyMessage dynaConfigLegacyMessage = DynaConfigLegacyMessage.builder()
                .applicationId(message.getAppId())
                .domain(message.getDomain())
                .env(message.getEnv())
                //.instanceId(message.getId()) // fixme -> use messageCache?
                .messageId(message.getId())
                .internalType("INSTANCE")
                .serviceId("dynaconfig")
                .message(
                        InstanceInitPropertiesUpdateRequestDto.builder()
                                .type("com.accor.wcp.sdk.service.dynaconfig.communication.InstanceInitPropertiesUpdateRequestDto")
                                .updates(propertyUpdateMessageRequestDtos)
                                .build()
                )
                .build();

        String path = formatFilename(
                message.getDomain(),
                message.getEnv(),
                message.getAppId()
        );
        createFolders(path);

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

    private void createFolders(String path) {
        StringBuilder current = new StringBuilder();
        for (String segment : path.split("/")) {
            current.append(segment).append("/");
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(current.toString())
                            .contentType("application/x-directory")
                            .build(),
                    RequestBody.empty()
            );
        }
    }

    static String formatFilename(String domain, String env, String appId) {
        return "%s/%s/%s".formatted(domain, env, appId);
    }
}
