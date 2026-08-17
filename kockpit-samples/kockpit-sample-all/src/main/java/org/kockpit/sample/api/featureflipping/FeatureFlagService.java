package org.kockpit.sample.api.featureflipping;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.kockpit.communication.Message;
import org.kockpit.core.sdk.OnMessageListener;
import org.kockpit.features.featureflipping.service.FeatureFlippingDto;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static org.kockpit.features.featureflipping.service.FeatureFlippingServiceDefinition.FEATURE_FLIPPING;

@Component
@RequiredArgsConstructor
public class FeatureFlagService implements OnMessageListener {

    @Getter
    private final Map<String, Boolean> keys = new HashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onMessage(Message message) {
        if (message.getService().equals(FEATURE_FLIPPING)) {
            FeatureFlippingDto featureFlippingDto = objectMapper
                    .convertValue(message.getBody(), FeatureFlippingDto.class);
            keys.put(featureFlippingDto.getKey(), featureFlippingDto.getEnabled());
        }
    }

    Boolean get(String key) {
        return keys.getOrDefault(key, false);
    }
}
