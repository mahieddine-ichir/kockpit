package org.kockpit.sample.api.featureflipping;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.kockpit.communication.Message;
import org.kockpit.communication.MessageCache;
import org.kockpit.core.sdk.ServiceDefinition;
import org.kockpit.service.featureflipping.api.dto.FeatureFlippingDto;
import org.springframework.stereotype.Component;

import static org.kockpit.service.featureflipping.api.FeatureFlippingService.FEATURE_FLIPPING;

@Component
@RequiredArgsConstructor
public class FeatureFlippingEvaluatorService implements ServiceDefinition {

    private final MessageCache messageCache;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Boolean evaluate(String key) {
        return messageCache.get(FEATURE_FLIPPING, key)
                .map(Message::getBody)
                .map(o -> objectMapper.convertValue(o, FeatureFlippingDto.class))
                .map(FeatureFlippingDto::getEnabled)
                .orElse(false);
    }

    @Override
    public String name() {
        return "FeatureFlipping";
    }
}
