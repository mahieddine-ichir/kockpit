package org.kockpit.features.heartbeat.services.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.kockpit.communication.Message;
import org.kockpit.communication.MessageCache;
import org.kockpit.core.sdk.ServiceDefinition;
import org.kockpit.features.heartbeat.services.HeartBeatDto;
import org.kockpit.features.heartbeat.services.HeartBeatServiceDefinition;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/heartbeat")
@RequiredArgsConstructor
public class HeartBeatApi {

    private final MessageCache messageCache;

    private final ServiceDefinition serviceDefinition = new HeartBeatServiceDefinition(true);

    @GetMapping
    List<Message> heartBeats() {
        return messageCache.get(serviceDefinition.name()).stream()
                .toList();
    }
}
