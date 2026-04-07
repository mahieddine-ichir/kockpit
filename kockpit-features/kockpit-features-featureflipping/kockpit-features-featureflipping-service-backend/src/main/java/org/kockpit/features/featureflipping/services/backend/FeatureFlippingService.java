package org.kockpit.features.featureflipping.services.backend;

import lombok.RequiredArgsConstructor;
import org.kockpit.communication.Message;
import org.kockpit.communication.Publisher;
import org.kockpit.features.featureflipping.service.FeatureFlippingDto;
import org.kockpit.features.featureflipping.service.FeatureFlippingServiceDefinition;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FeatureFlippingService {

    private final List<Publisher> publishers;

    private final FeatureFlippingServiceDefinition featureFlippingServiceDefinition;

    public FeatureFlippingDto update(String domain, String env, String appId, FeatureFlippingDto featureFlippingDto) {
        publishers.forEach(publisher -> this.update(publisher, domain, env, appId, featureFlippingDto));
        return featureFlippingDto;
    }

    private void update(Publisher publisher, String domain, String env, String appId, FeatureFlippingDto featureFlippingDto) {
        publisher.publish(new Message(featureFlippingDto.getKey(), featureFlippingServiceDefinition.name(), domain, env, appId, featureFlippingDto, Map.of("audience", appId)));
    }

    public List<FeatureFlippingDto> getHistory(String domain, String env) {
        return List.of();
    }
}
