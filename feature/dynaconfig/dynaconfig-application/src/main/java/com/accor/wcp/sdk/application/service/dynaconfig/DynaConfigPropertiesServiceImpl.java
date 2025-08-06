package com.accor.wcp.sdk.application.service.dynaconfig;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;

@Slf4j
class DynaConfigPropertiesServiceImpl implements DynaConfigPropertiesService {
    private Map<String, Object> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Boolean> getBoolean(String property) {
        if (store.containsKey(property)) {
            Object obj = store.get(property);
            if (isNull(obj)) {
                log.warn("Property: {} value null (is not a boolean)! Skip and return null instead.", property);
                return Optional.empty();
            }
            return Optional.of(Boolean.valueOf(obj.toString()));
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getString(String property) {
        if (store.containsKey(property)) {
            Object obj = store.get(property);
            if (obj instanceof String s) {
                return Optional.of(s);
            } else {
                log.warn("Property: {} value {} is not a string! Skip and return null instead.", property, obj);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Integer> getInteger(String property) {
        if (store.containsKey(property)) {
            Object obj = store.get(property);
            if (obj instanceof Integer i) {
                return Optional.of(i);
            } else {
                log.warn("Property: {} value {} is not an integer! Skip and return null instead.", property, obj);
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

    void updatePropertyWithNewValue(String propertyName, String newValue) {
        store.put(propertyName, newValue);
    }
}
