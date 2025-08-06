package com.accor.wcp.sdk.application.service.featureflipping;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;

@Slf4j
class FeatureFlippingKeysServiceImpl implements FeatureFlippingKeysService {
    private Map<String, Object> store = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> storeEvaluationConfig = new ConcurrentHashMap<>();

    @Override
    public Optional<Boolean> getBoolean(String key) {
        if (store.containsKey(key)) {
            Object obj = store.get(key);
            if (isNull(obj)) {
                log.warn("Property: {} value null (is not a boolean)! Skip and return null instead.", key);
                return Optional.empty();
            }
            return Optional.of(Boolean.valueOf(obj.toString()));
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getString(String key) {
        if (store.containsKey(key)) {
            Object obj = store.get(key);
            if (obj instanceof String s) {
                return Optional.of(s);
            } else {
                log.warn("Property: {} value {} is not a string! Skip and return null instead.", key, obj);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Integer> getInteger(String key) {
        if (store.containsKey(key)) {
            Object obj = store.get(key);
            if (obj instanceof Integer i) {
                return Optional.of(i);
            } else {
                log.warn("Property: {} value {} is not an integer! Skip and return null instead.", key, obj);
            }
        }
        return Optional.empty();
    }

    void initializeWithLocalPropertiesValues(Map<String, Set<Object>> initialPropertyValues) {
        store = new ConcurrentHashMap<>(initialPropertyValues.size());
        initialPropertyValues.forEach((s,objects) -> {
            // Keep only first value
            objects.stream().filter(Objects::nonNull).findFirst().ifPresent(o -> store.put(s, o));
        });
    }

    void updatePropertyWithNewValue(String propertyName, String newValue, Map<String, String> evaluationConfig) {
        if (isNull(newValue)) {
            log.warn("Property: {} value is null! Skip it.", propertyName);
            return;
        }
        store.put(propertyName, newValue);
        if (isNull(evaluationConfig)) {
            return;
        }
        storeEvaluationConfig.put(propertyName, evaluationConfig);
    }

    @Override
    public Optional<Map<String, String>> getEvaluationConfig(String key) {
        if (storeEvaluationConfig.containsKey(key)) {
            return Optional.of(storeEvaluationConfig.get(key));
        }
        return Optional.empty();
    }
}
