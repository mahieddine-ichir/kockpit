package org.kockpit.service.featureflipping.api;

import lombok.RequiredArgsConstructor;
import org.kockpit.communication.Message;
import org.kockpit.communication.Publisher;
import org.kockpit.core.sdk.ServiceDefinition;
import org.kockpit.service.featureflipping.api.dto.FeatureFlippingDto;
import org.kockpit.service.featureflipping.api.dto.FeatureFlippingHistory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FeatureFlippingService implements ServiceDefinition {

    public static final String FEATURE_FLIPPING = "FeatureFlipping";

    private final Publisher publisher;

    @Override
    public String name() {
        return FEATURE_FLIPPING;
    }

    public FeatureFlippingDto update(String domain, String env, String appId, FeatureFlippingDto featureFlippingDto) {
        publisher.publish(new Message(featureFlippingDto.getKey(), name(), domain, env, appId, featureFlippingDto));
        return featureFlippingDto;
    }

    public List<FeatureFlippingHistory> getHistory(String domain, String env) {
        return List.of();
    }
}
