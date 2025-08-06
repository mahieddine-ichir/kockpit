package com.accor.wcp.console.services.dynaconfig.instance;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;

import com.accor.wcp.console.services.dynaconfig.instance.communication.dto.DynaConfigRequest;
import com.accor.wcp.console.services.dynaconfig.instance.domain.DynaConfigInstance;
import com.accor.wcp.sdk.service.dynaconfig.communication.DynaConfigOperationResult;
import com.accor.wcp.sdk.service.dynaconfig.communication.PropertyUpdateMessageRequestDto;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DynaConfigInstanceManager {

  private final Map<String, Map<String, Map<String, Map<String, Set<DynaConfigInstance>>>>>
      instancePropertiesByDomainEnvApp = new ConcurrentHashMap<>();

  public void resetInstancesPropertiesMap(String domain, String env, String appId) {
    instancePropertiesByDomainEnvApp
        .getOrDefault(domain, new ConcurrentHashMap<>())
        .getOrDefault(env, new ConcurrentHashMap<>())
        .remove(appId);
  }

  public void resetInstancesPropertiesMap(
      String domain, String env, String appId, String instanceId) {
    instancePropertiesByDomainEnvApp
        .getOrDefault(domain, new ConcurrentHashMap<>())
        .getOrDefault(env, new ConcurrentHashMap<>())
        .getOrDefault(appId, new ConcurrentHashMap<>())
        .remove(instanceId);
  }

  public Set<DynaConfigInstance> initInstanceProperties(
      String domain,
      String env,
      String appId,
      String instanceId,
      Map<String, Object> inputProperties) {
    if (Objects.isNull(inputProperties)) {
      return emptySet();
    }

    Map<String, Set<DynaConfigInstance>> properties =
        instancePropertiesByDomainEnvApp
            .computeIfAbsent(domain, s -> new ConcurrentHashMap<>())
            .computeIfAbsent(env, s -> new ConcurrentHashMap<>())
            .computeIfAbsent(appId, s -> new ConcurrentHashMap<>());

    List<DynaConfigInstance> inputInstanceProperties =
        inputProperties.entrySet().stream()
            .filter(entry -> Objects.nonNull(entry.getValue()) && Objects.nonNull(entry.getKey()))
            .map(entry -> buildInstanceProperty(instanceId, entry.getKey(), entry.getValue()))
            .toList();

    Set<DynaConfigInstance> instanceProperties =
        properties.getOrDefault(instanceId, ConcurrentHashMap.newKeySet());
    instanceProperties.addAll(inputInstanceProperties);
    properties.put(instanceId, instanceProperties);
    return instanceProperties;
  }

  private DynaConfigInstance buildInstanceProperty(String id, String key, Object value) {
    String currentValue;
    if (value instanceof List listValues) {
      currentValue = Arrays.toString(listValues.toArray()).replace("[", "").replace("]", "");
    } else {
      currentValue = value.toString();
    }

    return DynaConfigInstance.builder()
        .propertyKey(key)
        .currentValue(currentValue)
        .applicationInstance(id)
        .build();
  }

  public void updateInstanceStatus(
      String domain,
      String env,
      String instanceId,
      String propertyName,
      DynaConfigRequest request,
      DynaConfigOperationResult result) {
    List<PropertyUpdateMessageRequestDto> propertiesUpdated = request.getMessages();
    String appId = request.getAppId();

    if (result == DynaConfigOperationResult.DONE) {
      propertiesUpdated.forEach(
          propertyUpdated -> {
            if (propertyUpdated.getPropertyName().equals(propertyName)) {
              String updatedValue = propertyUpdated.getNewValue();

              Optional<DynaConfigInstance> instanceProperty =
                  getLastKnownInstanceProperty(domain, env, appId, instanceId, propertyName);
              if (instanceProperty.isPresent()) {
                instanceProperty.get().setCurrentValue(updatedValue);
              } else {
                this.initInstanceProperties(
                    domain, env, appId, instanceId, singletonMap(propertyName, updatedValue));
              }
            }
          });
    }
  }

  public Optional<DynaConfigInstance> getLastKnownInstanceProperty(
      String domain, String env, String app, String idInstance, String propertyKey) {
    return this.getInstancesProperties(domain, env, app).stream()
        .filter(instance -> idInstance.equals(instance.getApplicationInstance()))
        .filter(instance -> propertyKey.equals(instance.getPropertyKey()))
        .findFirst();
  }

  public List<DynaConfigInstance> getInstancesProperties(String domain, String env, String app) {
    Map<String, Set<DynaConfigInstance>> propertiesByInstance =
        instancePropertiesByDomainEnvApp
            .getOrDefault(domain, Collections.emptyMap())
            .getOrDefault(env, Collections.emptyMap())
            .getOrDefault(app, Collections.emptyMap());

    return propertiesByInstance.values().stream().flatMap(Collection::stream).toList();
  }
}
