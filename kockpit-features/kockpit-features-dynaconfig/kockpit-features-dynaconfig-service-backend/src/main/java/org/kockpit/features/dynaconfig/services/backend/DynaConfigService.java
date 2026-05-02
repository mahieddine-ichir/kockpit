package org.kockpit.features.dynaconfig.services.backend;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.KeyValue;
import org.kockpit.communication.Message;
import org.kockpit.communication.Publisher;
import org.kockpit.core.sdk.OnMessageListener;
import org.kockpit.features.dynaconfig.service.DynaConfigDto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;
import static org.kockpit.features.dynaconfig.service.DynaConfigServiceDefinition.DYNA_CONFIG;

@Component
@RequiredArgsConstructor
@Slf4j
public class DynaConfigService implements OnMessageListener {

    private final List<Publisher> publishers;

    private final Map<DynaConfigKey, DynaConfigDto> messagesCache = new ConcurrentHashMap<>();

    List<DynaConfigDto> get(String domain, String env, String appId) {
        return messagesCache.entrySet().stream()
                .filter(entry -> {
                    DynaConfigKey key = entry.getKey();
                    return key.domain.equals(domain) && key.env.equals(env) && key.appId.equals(appId);
                })
                .map(Map.Entry::getValue).toList();
    }

    DynaConfigDto update(String domain, String env, String appId, DynaConfigDto dynaConfigDto) {
        Message message = toMessage(domain, env, appId, dynaConfigDto);
        publishers.forEach(publisher -> {
            try  {
                publisher.publish(message);
                messagesCache.put(new DynaConfigKey(domain, env, appId, dynaConfigDto.getKey()), dynaConfigDto);
            } catch (Exception ex) {
                log.error("Error publishing message {}", message, ex);
            }
        });
        return dynaConfigDto;
    }

    @Override
    public void republish() {
        messagesCache.values().forEach(configDto -> publishers.forEach(publisher -> {
            Message message = toMessage(configDto.getDomain(), configDto.getEnv(), configDto.getAppId(), configDto);
            publisher.publish(message);
        }));
    }

    private Message toMessage(String domain, String env, String appId, DynaConfigDto dynaConfigDto) {
        return Message.builder()
                .appId(appId)
                .domain(domain)
                .env(env)
                .service(DYNA_CONFIG)
                .type("update")
                .id(dynaConfigDto.getKey())
                .body(dynaConfigDto)
                .keyValues(List.of(
                        new KeyValue(dynaConfigDto.getKey(), dynaConfigDto.getValue())
                ))
                .creationDate(Instant.now().toEpochMilli())
                .build();
    }

    @Override
    public void onMessage(Message message) {
        if (!message.getService().equals(DYNA_CONFIG)) {
            return;
        }
        if (message.getKeyValues() == null) {
            return;
        }
        message.getKeyValues().forEach(kv -> {
            DynaConfigKey dynaConfigKey = new DynaConfigKey(message.getDomain(), message.getEnv(), message.getAppId(), kv.key());
            if ("update".equals(message.getType()) ||
                    (isNull(message.getType()) && !messagesCache.containsKey(dynaConfigKey))
            ) {
                messagesCache.put(dynaConfigKey, DynaConfigDto
                        .builder()
                        .domain(message.getDomain())
                        .env(message.getEnv())
                        .appId(message.getAppId())
                        .key(kv.key())
                        .value(kv.value())
                        .build());
            }
        });
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    static class DynaConfigKey {
        String domain;
        String env;
        String appId;
        String key;
    }

}
