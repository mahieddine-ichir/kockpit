package org.kockpit.features.dynaconfig.services.backend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.KeyValue;
import org.kockpit.communication.Message;
import org.kockpit.communication.Publisher;
import org.kockpit.features.dynaconfig.service.DynaConfigDto;
import org.kockpit.features.dynaconfig.service.DynaConfigServiceDefinition;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DynaConfigService {

    private final List<Publisher> publishers;

    private final DynaConfigServiceDefinition dynaConfigServiceDefinition;

    DynaConfigDto update(String domain, String env, String appId, DynaConfigDto dynaConfigDto) {
        publishers.forEach(publisher ->
                silentPublish(publisher,
                    Message.builder()
                            .appId(appId)
                            .domain(domain)
                            .env(env)
                            .type(dynaConfigServiceDefinition.name())
                            .id(dynaConfigDto.getKey())
                            .keyValues(List.of(
                                    new KeyValue(dynaConfigDto.getKey(), dynaConfigDto.getValue())
                            ))
                            .creationDate(Instant.now().toEpochMilli())
                            .build()
                        ));
        return dynaConfigDto;
    }

    private void silentPublish(Publisher publisher, Message message) {
        try  {
            publisher.publish(message);
        } catch (Exception ex) {
            log.error("Error publishing message {}", message, ex);
        }
    }
}
