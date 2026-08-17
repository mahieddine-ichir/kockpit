package org.kockpit.communication.legacy.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Consumer;
import org.kockpit.communication.Message;
import software.amazon.awssdk.services.s3.S3Client;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class HealthLegacyConsumer implements Consumer {

    private final S3Client s3Client;

    private final String bucketName;

    private final ObjectMapper objectMapper;

    private final HealthLegacyMapper mapper = new HealthLegacyMapper();

    @Override
    public List<Message> poll(String domain, String env, String appId, String type) {
        return Collections.emptyList();
    }

}
