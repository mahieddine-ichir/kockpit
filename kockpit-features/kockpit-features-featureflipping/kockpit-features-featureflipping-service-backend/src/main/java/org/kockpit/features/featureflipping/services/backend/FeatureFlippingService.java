package org.kockpit.features.featureflipping.services.backend;

import lombok.RequiredArgsConstructor;
import org.kockpit.communication.Message;
import org.kockpit.communication.Publisher;
import org.kockpit.features.featureflipping.service.FeatureFlippingDto;
import org.kockpit.features.featureflipping.service.FeatureFlippingServiceDefinition;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FeatureFlippingService {

    private final Publisher publisher;

    private final FeatureFlippingServiceDefinition featureFlippingServiceDefinition;

    public FeatureFlippingDto update(String domain, String env, String appId, FeatureFlippingDto featureFlippingDto) {
        publisher.publish(new Message(featureFlippingDto.getKey(), featureFlippingServiceDefinition.name(), domain, env, appId, featureFlippingDto));
        return featureFlippingDto;
    }

    public List<FeatureFlippingDto> getHistory(String domain, String env) {
        return List.of();
    }
}
