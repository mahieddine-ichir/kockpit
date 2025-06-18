package org.kockpit.audit.obfuscation.impl;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;

import org.kockpit.audit.obfuscation.Obfuscate;
import org.kockpit.audit.obfuscation.ObfuscateConfig;
import org.kockpit.audit.obfuscation.ObfuscationService;
import org.kockpit.audit.obfuscation.ObjectObfuscateConfig;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.core.ResolvableType;

@Slf4j
class ObfuscationServiceImpl implements ObfuscationService {

  private final Map<Class<?>, Obfuscate<?>> obfuscateByConfigurationClass;

  ObfuscationServiceImpl(List<Obfuscate<?>> obfuscates) {
    obfuscateByConfigurationClass = obfuscates.stream()
        .map(this::createClassObfuscateEntry)
        .filter(Objects::nonNull)
        .collect(toMap(Entry::getKey, Entry::getValue));
  }

  private Entry<Class<?>, ? extends Obfuscate<?>> createClassObfuscateEntry(
      Obfuscate<?> obfuscate) {
    ResolvableType configurationType = ResolvableType.forClass(obfuscate.getClass())
        .as(Obfuscate.class)
        .getGeneric(0);
    Class<?> configurationClass = configurationType.getRawClass();
    if (isNull(configurationClass)) {
      log.warn("Obfuscate implementation: {} does not defined linked configuration class. Skipping it.", obfuscate.getClass());
      return null;
    }
    return Map.entry(configurationClass, obfuscate);
  }

  @Override
  public String obfuscate(String data, ObfuscateConfig config) {
    Class<? extends ObfuscateConfig> configClass = config.getClass();
    Obfuscate<?> obfuscate = obfuscateByConfigurationClass.get(configClass);
    if (isNull(obfuscate)) {
      log.warn("Obfuscate implementation for configuration: {} not found. Skipping obfuscation.", configClass);
      return data;
    }

    return obfuscate.obfuscate(data, config);
  }

  @Override
  public <T> T obfuscateObject(T data, ObjectObfuscateConfig config) {
    if (isNull(data) || isNull(config) || isNull(config.getObfuscateConfigByProperty())) {
      return data;
    }

    config.getObfuscateConfigByProperty().forEach((propertyPath, obfuscateConfig) -> {
      Object propertyData;
      try {
        propertyData = PropertyUtils.getNestedProperty(data, propertyPath);
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        log.warn("Can not get property: {} from object of class: {}", propertyPath, data.getClass());
        return;
      }

      if (isNull(propertyData)) {
        return;
      }

      // Obfuscate
      Object obfuscated;
      if (propertyData instanceof String) {
        obfuscated = obfuscate(propertyData.toString(), obfuscateConfig);
      } else if (isMapOfString(propertyData)) {
        obfuscated = obfuscateMap((Map<String, Object>) propertyData, obfuscateConfig);
      } else {
        log.warn("propertyData found is not a String type or Map of String type ({}). Default behavior use .toString().", propertyData.getClass());
        return;
      }
      try {
        PropertyUtils.setNestedProperty(data, propertyPath, obfuscated);
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        log.warn("Can not set back obfuscated value to object. Error={}", e.getMessage());
      }
    });

    return data;
  }

  private boolean isMapOfString(Object o) {
    if (o instanceof Map<?, ?> map && !map.isEmpty()) {
      Entry<?, ?> entry = map.entrySet().iterator().next();
      return entry.getKey() instanceof String;
    }
    return false;
  }

  private Map<String, Object> obfuscateMap(Map<String, Object> data, ObfuscateConfig config) {
    if (config instanceof ObjectObfuscateConfig objectObfuscateConfig) {
      objectObfuscateConfig.getObfuscateConfigByProperty().forEach((key, obfuscateConfig) -> {
        if (data.containsKey(key)) {
          Object entryValue = data.get(key);
          if (entryValue instanceof List<?> dataList) {
            List<String> list = dataList.stream().map(o -> obfuscate(o.toString(), obfuscateConfig))
                .toList();
            data.put(key, list);
          } else {
            data.put(key, obfuscate(entryValue.toString(), obfuscateConfig));
          }

        } else {
          log.warn("Can not get key: {} from Map", key);
        }
      });
    }
    return data;
  }
}
