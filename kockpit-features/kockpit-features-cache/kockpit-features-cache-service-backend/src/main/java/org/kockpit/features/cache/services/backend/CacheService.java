package org.kockpit.features.cache.services.backend;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Message;
import org.kockpit.communication.Publisher;
import org.kockpit.core.sdk.OnMessageListener;
import org.kockpit.features.cache.service.CacheStatsDto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.kockpit.features.cache.service.CacheServiceDefinition.CACHE;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheService implements OnMessageListener {

    private final List<Publisher> publishers;

    private final Map<CacheKey, CacheStatsDto> cache = new ConcurrentHashMap<>();

    public List<CacheStatsDto> get(String domain, String env, String appId) {
        return cache.entrySet().stream()
                .filter(e -> e.getKey().domain.equals(domain) && e.getKey().env.equals(env) && e.getKey().appId.equals(appId))
                .map(Map.Entry::getValue)
                .toList();
    }

    public void clearCache(String domain, String env, String appId) {
        Message message = Message.builder()
                .service(CACHE)
                .type("reset")
                .domain(domain)
                .env(env)
                .appId(appId)
                .creationDate(Instant.now().toEpochMilli())
                .build();
        publishers.forEach(publisher -> {
            try {
                publisher.publish(message);
            } catch (Exception ex) {
                log.error("Error publishing cache reset message", ex);
            }
        });
        log.info("Cache reset published for {}/{}/{}", domain, env, appId);
    }

    public void resetStats(String domain, String env, String appId) {
        cache.keySet().removeIf(k -> k.domain.equals(domain) && k.env.equals(env) && k.appId.equals(appId));
        log.info("Cache stats reset for {}/{}/{}", domain, env, appId);
    }

    @Override
    public void onMessage(Message message) {
        if (!CACHE.equals(message.getService())) {
            return;
        }
        if (!(message.getBody() instanceof Map<?, ?> bodyMap)) {
            return;
        }
        String name = str(bodyMap.get("name"));
        if (name == null) {
            return;
        }
        CacheKey key = new CacheKey(message.getDomain(), message.getEnv(), message.getAppId(), name);
        cache.put(key, CacheStatsDto.builder()
                .domain(message.getDomain())
                .env(message.getEnv())
                .appId(message.getAppId())
                .name(name)
                .timestamp(longVal(bodyMap.get("timestamp")))
                .cacheGets(longVal(bodyMap.get("cacheGets")))
                .cacheHits(longVal(bodyMap.get("cacheHits")))
                .cacheMisses(longVal(bodyMap.get("cacheMisses")))
                .cacheRemovals(longVal(bodyMap.get("cacheRemovals")))
                .cacheEvictions(longVal(bodyMap.get("cacheEvictions")))
                .averageGetTime(doubleVal(bodyMap.get("averageGetTime")))
                .averagePutTime(doubleVal(bodyMap.get("averagePutTime")))
                .averageRemoveTime(doubleVal(bodyMap.get("averageRemoveTime")))
                .cacheHitPercentage(doubleVal(bodyMap.get("cacheHitPercentage")))
                .cacheMissPercentage(doubleVal(bodyMap.get("cacheMissPercentage")))
                .build());
        log.trace("Cache stats updated for {}/{}/{}/{}", message.getDomain(), message.getEnv(), message.getAppId(), name);
    }

    private String str(Object v) {
        return v != null ? v.toString() : null;
    }

    private Long longVal(Object v) {
        if (v instanceof Number n) return n.longValue();
        return null;
    }

    private Double doubleVal(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        return null;
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    static class CacheKey {
        String domain;
        String env;
        String appId;
        String name;
    }
}
