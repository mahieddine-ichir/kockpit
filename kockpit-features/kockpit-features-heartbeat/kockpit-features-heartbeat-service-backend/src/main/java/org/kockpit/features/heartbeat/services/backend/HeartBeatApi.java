package org.kockpit.features.heartbeat.services.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.kockpit.communication.Message;
import org.kockpit.communication.MessageCache;
import org.kockpit.features.heartbeat.services.HeartBeatDto;
import org.kockpit.features.heartbeat.services.HeartBeatServiceDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/{domain}/{env}/heartbeat")
@RequiredArgsConstructor
public class HeartBeatApi {

    private final MessageCache messageCache;

    private final ObjectMapper objectMapper;

    @Value("${kockpit.heartbeat.active-threshold-minutes:10}")
    private long activeThresholdMinutes;

    @GetMapping
    List<Message> heartBeats(
            @PathVariable String domain,
            @PathVariable String env
    ) {
        return messageCache.get(HeartBeatServiceDefinition.SERVICE_NAME).stream()
                .filter(message -> message.getDomain().equals(domain) && message.getEnv().equals(env))
                .map(message -> {
                    HeartBeatDto heartBeatDto = objectMapper.convertValue(message.getBody(), HeartBeatDto.class);
                    return Message.builder()
                            .appId(message.getAppId())
                            .type(message.getType())
                            .domain(message.getDomain())
                            .env(message.getEnv())
                            .id(message.getId())
                            .body(heartBeatDto)
                            .build();
                })
                //.filter(message -> new Date(message.getCreationDate()).toInstant().isAfter(Instant.now().minus(activeThresholdMinutes, ChronoUnit.MINUTES)))
                .toList();
    }

}
