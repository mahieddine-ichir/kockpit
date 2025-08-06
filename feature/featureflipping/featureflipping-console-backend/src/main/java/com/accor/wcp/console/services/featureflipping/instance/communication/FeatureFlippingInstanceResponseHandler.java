package com.accor.wcp.console.services.featureflipping.instance.communication;

import com.accor.wcp.console.sdk.service.ApplicationMessageNotification;
import com.accor.wcp.console.services.featureflipping.instance.FeatureFlippingInstanceManager;
import com.accor.wcp.console.services.featureflipping.instance.FeatureFlippingSynchronizeInstanceService;
import com.accor.wcp.console.services.featureflipping.instance.communication.dto.FeatureFlippingRequest;
import com.accor.wcp.console.services.featureflipping.instance.communication.dto.FeatureFlippingResponse;
import com.accor.wcp.console.services.featureflipping.instance.domain.FeatureFlippingInstance;
import com.accor.wcp.console.services.featureflipping.manifest.FeatureFlippingPropertySettings;
import com.accor.wcp.console.services.featureflipping.manifest.FeatureFlippingServiceManifest;
import com.accor.wcp.console.services.featureflipping.manifest.FeatureFlippingSettingsDto;
import com.accor.wcp.sdk.service.featureflipping.communication.FeatureFlippingOperationResult;
import com.accor.wcp.sdk.service.featureflipping.communication.InstanceInitPropertiesMessageDto;
import com.accor.wcp.sdk.service.featureflipping.communication.InstanceInitPropertiesUpdateResponseDto;
import com.accor.wcp.sdk.service.featureflipping.communication.PropertyUpdateMessageResponseDto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Component
public class FeatureFlippingInstanceResponseHandler {
  private final FeatureFlippingInstanceManager instanceManager;
  private final FeatureFlippingSynchronizeInstanceService featureFlippingSynchronizeInstanceService;
  private final FeatureFlippingInstanceRequestHandler requestHandler;
  private final ObjectMapper objectMapper;

  public FeatureFlippingInstanceResponseHandler(
      FeatureFlippingInstanceManager instanceManager,
      FeatureFlippingSynchronizeInstanceService featureFlippingSynchronizeInstanceService,
      FeatureFlippingInstanceRequestHandler requestHandler) {
    objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

    this.instanceManager = instanceManager;
    this.featureFlippingSynchronizeInstanceService = featureFlippingSynchronizeInstanceService;
    this.requestHandler = requestHandler;
  }

  public void handleInstanceUpdateResponse(ApplicationMessageNotification notification) {
    log.debug("updatePropertyResponse: {}", notification);
    PropertyUpdateMessageResponseDto message =
        getMessage(notification, PropertyUpdateMessageResponseDto.class);

    String domain = notification.getDomain();
    String env = notification.getEnv();
    String instanceId = notification.getInstanceId();
    String requestMessageId = notification.getRequestMessageId();
    FeatureFlippingOperationResult result = message.getResult();
    String propertyName = message.getPropertyName();

    FeatureFlippingResponse responseDto = buildDynaConfigResponse(requestMessageId, instanceId, message);
    FeatureFlippingRequest request = requestHandler.linkResponseToRequest(requestMessageId, responseDto);

    if (nonNull(request)) {
      instanceManager.updateInstanceStatus(domain, env, instanceId, propertyName, request, result);
    }
  }

  public void handleInstanceInitProperties(
      ApplicationMessageNotification notification, FeatureFlippingServiceManifest manifest) {
    log.debug("instanceInitProperties: {}", notification);
    Map<String, Object> props = getInstanceProperties(notification);

    String domain = notification.getDomain();
    String env = notification.getEnv();
    String appId = notification.getApplicationId();
    String instanceId = notification.getInstanceId();

    // Ignore instance properties not defined in manifest
    props.keySet().removeIf(k -> !getManifestProperties(domain, env, appId, manifest).contains(k));

    // Keep in WCP context instance properties values
    Set<FeatureFlippingInstance> instanceProps =
        instanceManager.initInstanceProperties(domain, env, appId, instanceId, props);

    // Sync instance with wcp override values
    featureFlippingSynchronizeInstanceService.syncInstance(instanceId, domain, env, appId, instanceProps);
  }

  private Map<String, Object> getInstanceProperties(ApplicationMessageNotification notification) {
    InstanceInitPropertiesMessageDto message =
        getMessage(notification, InstanceInitPropertiesMessageDto.class);
    if (Objects.isNull(message) || isEmpty(message.getPropertyValues())) {
      return emptyMap();
    }

    return message.getPropertyValues().entrySet().stream()
// [FIX] WCP-109 => must not remove property is null value (synchronization issue)
//        .filter(e -> nonNull(e.getValue()))
//        .filter(e -> nonNull(firstElement(e.getValue())))
        .collect(toMap(Entry::getKey, entry -> Optional.ofNullable(firstElement(entry.getValue())).orElse("")));
  }

  public void handleInstanceMultiUpdatesResponse(ApplicationMessageNotification notification) {
    log.debug("multiUpdatesPropertyResponse: {}", notification);
    InstanceInitPropertiesUpdateResponseDto message =
        getMessage(notification, InstanceInitPropertiesUpdateResponseDto.class);

    for (PropertyUpdateMessageResponseDto updateResult : message.getResults()) {
      String domain = notification.getDomain();
      String env = notification.getEnv();
      String instanceId = notification.getInstanceId();
      String requestMessageId = notification.getRequestMessageId();
      FeatureFlippingOperationResult result = updateResult.getResult();
      String propertyName = updateResult.getPropertyName();

      FeatureFlippingResponse responseDto =
          buildDynaConfigResponse(requestMessageId, instanceId, updateResult);
      FeatureFlippingRequest request =
          requestHandler.linkResponseToRequest(requestMessageId, responseDto);

      if (nonNull(request)) {
        instanceManager.updateInstanceStatus(
            domain, env, instanceId, propertyName, request, result);
      }
    }
  }

  public <T> T getMessage(ApplicationMessageNotification notification, Class<T> type) {
    return objectMapper.convertValue(notification.getMessage(), type);
  }

  private FeatureFlippingResponse buildDynaConfigResponse(
      String id, String instanceId, PropertyUpdateMessageResponseDto response) {
    return FeatureFlippingResponse.builder()
        .requestId(id)
        .instanceId(instanceId)
        .message(response)
        .timestamp(System.currentTimeMillis())
        .build();
  }

  private List<String> getManifestProperties(
      String domain, String env, String appId, FeatureFlippingServiceManifest manifest) {
    String manifestKey = domain + "-" + env + "-" + appId;
    FeatureFlippingSettingsDto setting = manifest.getFeatureFlippingSettingsMap().get(manifestKey);

    return Optional.ofNullable(setting)
        .map(FeatureFlippingSettingsDto::getKeys)
        .orElse(emptyList())
        .stream()
        .map(FeatureFlippingPropertySettings::getKey)
        .toList();
  }

  private List<String> getManifestPropertiesForAppId(
      String domain, String env, String appId, FeatureFlippingServiceManifest manifest) {
    String manifestKey = domain + "-" + env + "-" + appId;
    FeatureFlippingSettingsDto setting = manifest.getFeatureFlippingSettingsMap().get(manifestKey);

    return Optional.ofNullable(setting)
        .map(FeatureFlippingSettingsDto::getKeys)
        .orElse(emptyList())
        .stream()
        .map(FeatureFlippingPropertySettings::getKey)
        .toList();
  }

}
