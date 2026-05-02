package org.kockpit.features.heartbeat.services.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.kockpit.communication.Message;
import org.kockpit.features.heartbeat.services.HeartBeatDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/{domain}/{env}/heartbeat")
@RequiredArgsConstructor
public class HeartBeatApi {

    private final HeartBeatManager heartBeatManager;

    private final ObjectMapper objectMapper;

    @GetMapping
    List<Message> heartBeats(
            @PathVariable String domain,
            @PathVariable String env
    ) {
        return heartBeatManager.get(domain, env).stream()
                .map(message -> Message.builder()
                        .appId(message.getAppId())
                        .type(message.getType())
                        .domain(message.getDomain())
                        .env(message.getEnv())
                        .id(message.getId())
                        .creationDate(message.getCreationDate())
                        .body(objectMapper.convertValue(message.getBody(), HeartBeatDto.class))
                        .build())
                .toList();
    }
}