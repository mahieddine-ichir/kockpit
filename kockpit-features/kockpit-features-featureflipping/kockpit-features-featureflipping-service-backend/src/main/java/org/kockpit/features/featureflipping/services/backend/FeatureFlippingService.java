package org.kockpit.features.featureflipping.services.backend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.KeyValue;
import org.kockpit.communication.Message;
import org.kockpit.communication.Publisher;
import org.kockpit.features.featureflipping.service.FeatureFlippingDto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static org.kockpit.features.featureflipping.service.FeatureFlippingServiceDefinition.FEATURE_FLIPPING;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeatureFlippingService {

    private final List<Publisher> publishers;

    public FeatureFlippingDto update(String domain, String env, String appId, FeatureFlippingDto featureFlippingDto) {
        publishers.forEach(publisher -> silentPublish(publisher,
                Message.builder()
                        .id(featureFlippingDto.getKey())
                        .service(FEATURE_FLIPPING)
                        .domain(domain)
                        .env(env)
                        .appId(appId)
                        .keyValues(List.of(
                                new KeyValue(featureFlippingDto.getKey(), Objects.toString(featureFlippingDto.getEnabled(), null))
                        ))
                        .creationDate(Instant.now().toEpochMilli())
                        .build()));
        return featureFlippingDto;
    }

    private void silentPublish(Publisher publisher, Message message) {
        try {
            publisher.publish(message);
        } catch (Exception ex) {
            log.error("Error publishing message {}", message, ex);
        }
    }

    public List<FeatureFlippingDto> getHistory(String domain, String env) {
        return List.of();
    }
}
