package org.kockpit.service.featureflipping;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.service.featureflipping.api.FeatureFlippingDto;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class FeatureFlippingInMemoryCache implements FeatureFlippingCache {

    private final ConcurrentHashMap<String, FeatureFlippingDto> cache = new ConcurrentHashMap<>();

    @Override
    public void add(FeatureFlippingDto featureFlippingDto) {
        log.trace("update/add new flag {}, enabled? {}", featureFlippingDto.getKey(), featureFlippingDto.getEnabled());
        cache.put(featureFlippingDto.getKey(), featureFlippingDto);
    }

    @Override
    public Optional<FeatureFlippingDto> getFlag(String key) {
        return Optional.ofNullable(cache.get(key));
    }
}
