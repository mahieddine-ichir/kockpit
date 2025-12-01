package org.kockpit.features.heartbeat.services.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.kockpit.communication.Message;
import org.kockpit.communication.MessageCache;
import org.kockpit.core.sdk.ServiceDefinition;
import org.kockpit.features.heartbeat.services.HeartBeatDto;
import org.kockpit.features.heartbeat.services.HeartBeatServiceDefinition;
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

    private final ServiceDefinition serviceDefinition = new HeartBeatServiceDefinition(true);

    @GetMapping
    List<Message> heartBeats(
            @PathVariable String domain,
            @PathVariable String env
    ) {
        return messageCache.get(serviceDefinition.name()).stream()
                .filter(message -> message.getDomain().equals(domain) && message.getEnv().equals(env))
                .toList();
    }
}
