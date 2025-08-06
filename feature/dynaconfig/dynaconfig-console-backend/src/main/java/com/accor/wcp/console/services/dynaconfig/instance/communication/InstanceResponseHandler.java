package com.accor.wcp.console.services.dynaconfig.instance.communication;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.accor.wcp.console.sdk.service.ApplicationMessageNotification;
import com.accor.wcp.console.services.dynaconfig.instance.DynaConfigInstanceManager;
import com.accor.wcp.console.services.dynaconfig.instance.SynchronizeInstanceService;
import com.accor.wcp.console.services.dynaconfig.instance.communication.dto.DynaConfigRequest;
import com.accor.wcp.console.services.dynaconfig.instance.communication.dto.DynaConfigResponse;
import com.accor.wcp.console.services.dynaconfig.instance.domain.DynaConfigInstance;
import com.accor.wcp.console.services.dynaconfig.manifest.DynaConfigPropertySettings;
import com.accor.wcp.console.services.dynaconfig.manifest.DynaConfigServiceManifest;
import com.accor.wcp.console.services.dynaconfig.manifest.DynaConfigSettingsDto;
import com.accor.wcp.sdk.service.dynaconfig.communication.DynaConfigOperationResult;
import com.accor.wcp.sdk.service.dynaconfig.communication.InstanceInitPropertiesMessageDto;
import com.accor.wcp.sdk.service.dynaconfig.communication.InstanceInitPropertiesUpdateResponseDto;
import com.accor.wcp.sdk.service.dynaconfig.communication.PropertyUpdateMessageResponseDto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InstanceResponseHandler {
  private final DynaConfigInstanceManager instanceManager;
  private final SynchronizeInstanceService synchronizeInstanceService;
  private final InstanceRequestHandler requestHandler;
  private final ObjectMapper objectMapper;

  public InstanceResponseHandler(
      DynaConfigInstanceManager instanceManager,
      SynchronizeInstanceService synchronizeInstanceService,
      InstanceRequestHandler requestHandler) {
    objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

    this.instanceManager = instanceManager;
    this.synchronizeInstanceService = synchronizeInstanceService;
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
    DynaConfigOperationResult result = message.getResult();
    String propertyName = message.getPropertyName();

    DynaConfigResponse responseDto = buildDynaConfigResponse(requestMessageId, instanceId, message);
    DynaConfigRequest request = requestHandler.linkResponseToRequest(requestMessageId, responseDto);

    if (nonNull(request)) {
      instanceManager.updateInstanceStatus(domain, env, instanceId, propertyName, request, result);
    }
  }

  public void handleInstanceInitProperties(
      ApplicationMessageNotification notification, DynaConfigServiceManifest manifest) {
    log.debug("instanceInitProperties: {}", notification);
    Map<String, Object> props = getInstanceProperties(notification);

    String domain = notification.getDomain();
    String env = notification.getEnv();
    String appId = notification.getApplicationId();
    String instanceId = notification.getInstanceId();

    // Ignore instance properties not defined in manifest
    props.keySet().removeIf(k -> !getManifestProperties(domain, env, appId, manifest).contains(k));

    // Keep in WCP context instance properties values
    Set<DynaConfigInstance> instanceProps =
        instanceManager.initInstanceProperties(domain, env, appId, instanceId, props);

    // Sync instance with wcp override values
    synchronizeInstanceService.syncInstance(instanceId, domain, env, appId, instanceProps);
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
      DynaConfigOperationResult result = updateResult.getResult();
      String propertyName = updateResult.getPropertyName();

      DynaConfigResponse responseDto =
          buildDynaConfigResponse(requestMessageId, instanceId, updateResult);
      DynaConfigRequest request =
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

  private DynaConfigResponse buildDynaConfigResponse(
      String id, String instanceId, PropertyUpdateMessageResponseDto response) {
    return DynaConfigResponse.builder()
        .requestId(id)
        .instanceId(instanceId)
        .message(response)
        .timestamp(System.currentTimeMillis())
        .build();
  }

  private List<String> getManifestProperties(
      String domain, String env, String appId, DynaConfigServiceManifest manifest) {
    String manifestKey = domain + "-" + env + "-" + appId;
    DynaConfigSettingsDto setting = manifest.getDynaConfigSettingsMap().get(manifestKey);

    return Optional.ofNullable(setting)
        .map(DynaConfigSettingsDto::getProperties)
        .orElse(emptyList())
        .stream()
        .map(DynaConfigPropertySettings::getName)
        .toList();
  }
}
