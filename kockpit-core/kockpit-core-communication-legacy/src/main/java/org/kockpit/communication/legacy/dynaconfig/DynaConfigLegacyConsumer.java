package org.kockpit.communication.legacy.dynaconfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Consumer;
import org.kockpit.communication.Message;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class DynaConfigLegacyConsumer implements Consumer {

    private final S3Client s3Client;

    private final String bucketName;

    private final ObjectMapper objectMapper;

    private final DynaConfigLegacyMapper mapper = new DynaConfigLegacyMapper();

    private Instant lastConsumption = Instant.EPOCH;

    @Override
    public List<Message> poll(String domain, String env, String appId, String type) {
        String prefix = DynaConfigLegacyPublisher.formatFilename(domain, env, appId) + "/";
        Instant pollTime = Instant.now();

        log.trace("Polling legacy messages for domain={}, env={}, appId={}, since={} (bucket={})", domain, env, appId, lastConsumption, bucketName);

        List<Message> messages = s3Client.listObjectsV2(
                        ListObjectsV2Request.builder()
                                .bucket(bucketName)
                                .prefix(prefix)
                                .build()
                )
                .contents()
                .stream()
                .filter(obj -> !obj.key().endsWith("/"))
                .filter(obj -> obj.lastModified().isAfter(lastConsumption))
                .map(this::read)
                .filter(Objects::nonNull)
                .toList();

        lastConsumption = pollTime;
        return messages;
    }

    private Message read(S3Object s3Object) {
        try {
            log.info("Reading legacy S3 object {}", s3Object.key());
            ResponseBytes<GetObjectResponse> bytes = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Object.key())
                            .build()
            );
            DynaConfigLegacyMessage legacy = objectMapper.readValue(bytes.asByteArray(), DynaConfigLegacyMessage.class);
            return mapper.toMessage(legacy);
        } catch (Exception e) {
            log.error("Error reading legacy S3 object {}", s3Object.key(), e);
            return null;
        }
    }
}
