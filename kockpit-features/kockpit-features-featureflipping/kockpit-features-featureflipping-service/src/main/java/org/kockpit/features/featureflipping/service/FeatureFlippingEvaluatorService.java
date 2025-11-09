package org.kockpit.features.featureflipping.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.kockpit.communication.Message;
import org.kockpit.communication.MessageCache;

@RequiredArgsConstructor
public class FeatureFlippingEvaluatorService {

    private final MessageCache messageCache;
    private final ObjectMapper objectMapper;

    private final FeatureFlippingServiceDefinition featureFlippingServiceDefinition;

    public Boolean evaluate(String key) {
        return messageCache.get(featureFlippingServiceDefinition.name(), key)
                .map(Message::getBody)
                .map(o -> objectMapper.convertValue(o, FeatureFlippingDto.class))
                .map(FeatureFlippingDto::getEnabled)
                .orElse(false);
    }

}
