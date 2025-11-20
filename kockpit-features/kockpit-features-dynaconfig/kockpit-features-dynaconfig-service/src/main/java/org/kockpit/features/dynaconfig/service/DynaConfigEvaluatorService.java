package org.kockpit.features.dynaconfig.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.kockpit.communication.Message;
import org.kockpit.communication.MessageCache;

@RequiredArgsConstructor
public class DynaConfigEvaluatorService {

    private final MessageCache messageCache;
    private final ObjectMapper objectMapper;

    private final DynaConfigServiceDefinition serviceDefinition;

    public Object evaluate(String key) {
        return messageCache.get(serviceDefinition.name(), key)
                .map(Message::getBody)
                .map(o -> objectMapper.convertValue(o, DynaConfigDto.class))
                .map(DynaConfigDto::getValue)
                .orElse(null);
    }

}
