package org.kockpit.features.featureflipping.services.backend;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.KeyValue;
import org.kockpit.communication.Message;
import org.kockpit.communication.Publisher;
import org.kockpit.core.sdk.OnMessageListener;
import org.kockpit.features.featureflipping.service.FeatureFlippingDto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;
import static org.kockpit.features.featureflipping.service.FeatureFlippingServiceDefinition.FEATURE_FLIPPING;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeatureFlippingService implements OnMessageListener {

    private final List<Publisher> publishers;

    private final Map<FeatureFlippingKey, FeatureFlippingDto> messagesCache = new ConcurrentHashMap<>();

    public List<FeatureFlippingDto> get(String domain, String env, String appId) {
        return messagesCache.entrySet().stream()
                .filter(entry -> {
                    FeatureFlippingKey key = entry.getKey();
                    return key.domain.equals(domain) && key.env.equals(env) && key.appId.equals(appId);
                })
                .map(Map.Entry::getValue).toList();
    }

    public FeatureFlippingDto update(String domain, String env, String appId, FeatureFlippingDto featureFlippingDto) {
        Message message = toMessage(domain, env, appId, featureFlippingDto);
        publishers.forEach(publisher -> {
            try  {
                publisher.publish(message);
                messagesCache.put(new FeatureFlippingKey(domain, env, appId, featureFlippingDto.getKey()), featureFlippingDto);
            } catch (Exception ex) {
                log.error("Error publishing message {}", message, ex);
            }
        });
        return featureFlippingDto;
    }

    private Message toMessage(String domain, String env, String appId, FeatureFlippingDto featureFlippingDto) {
        return Message.builder()
                .id(featureFlippingDto.getKey())
                .service(FEATURE_FLIPPING)
                .domain(domain)
                .env(env)
                .appId(appId)
                .keyValues(List.of(
                        new KeyValue(featureFlippingDto.getKey(), Objects.toString(featureFlippingDto.getEnabled(), null))
                ))
                .creationDate(Instant.now().toEpochMilli())
                .build();
    }

    @Override
    public void onMessage(Message message) {
        if (!message.getService().equals(FEATURE_FLIPPING)) {
            return;
        }
        if (message.getKeyValues() == null) {
            return;
        }
        message.getKeyValues().forEach(kv -> {
            FeatureFlippingKey dynaConfigKey = new FeatureFlippingKey(message.getDomain(), message.getEnv(), message.getAppId(), kv.key());
            if ("update".equals(message.getType()) ||
                    (isNull(message.getType()) && !messagesCache.containsKey(dynaConfigKey))
            ) {
                messagesCache.put(dynaConfigKey, FeatureFlippingDto
                        .builder()
                        .domain(message.getDomain())
                        .env(message.getEnv())
                        .appId(message.getAppId())
                        .key(kv.key())
                        //.enabled()
                        .build());
            }
        });
    }

    @Override
    public void republish() {
        messagesCache.values().forEach(flippingDto -> publishers.forEach(publisher -> {
            Message message = toMessage(flippingDto.getDomain(), flippingDto.getEnv(), flippingDto.getAppId(), flippingDto);
            publisher.publish(message);
        }));
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    static class FeatureFlippingKey {
        String domain;
        String env;
        String appId;
        String key;
    }

    public List<FeatureFlippingDto> getHistory(String domain, String env) {
        return List.of();
    }
}
