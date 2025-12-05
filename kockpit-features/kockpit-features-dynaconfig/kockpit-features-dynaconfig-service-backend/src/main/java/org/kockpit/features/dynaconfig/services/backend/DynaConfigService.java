package org.kockpit.features.dynaconfig.services.backend;

import lombok.RequiredArgsConstructor;
import org.kockpit.communication.Message;
import org.kockpit.communication.Publisher;
import org.kockpit.features.dynaconfig.service.DynaConfigDto;
import org.kockpit.features.dynaconfig.service.DynaConfigServiceDefinition;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DynaConfigService {

    private final Publisher publisher;

    private final DynaConfigServiceDefinition dynaConfigServiceDefinition;

    DynaConfigDto update(String domain, String env, String appId, DynaConfigDto dynaConfigDto) {
        publisher.publish(new Message(dynaConfigDto.getKey(), dynaConfigServiceDefinition.name(), domain, env, appId, dynaConfigDto, Map.of("audience", appId)));
        return dynaConfigDto;
    }
}
