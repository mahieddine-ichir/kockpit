package com.accor.wcp.console.services.dynaconfig.instance;

import static com.accor.wcp.sdk.service.dynaconfig.ServiceDefinition.SERVICE_ID;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;

import com.accor.wcp.console.sdk.notification.NotificationLevelType;
import com.accor.wcp.console.sdk.notification.UserNotification;
import com.accor.wcp.console.sdk.notification.WCPConsoleUserNotificationService;
import com.accor.wcp.console.sdk.notification.impl.DefaultUserNotification;
import com.accor.wcp.console.sdk.topology.ApplicationInstanceManager;
import com.accor.wcp.console.sdk.topology.ManagedInstanceEvent;
import com.accor.wcp.console.sdk.topology.ManagedInstanceListener;
import com.accor.wcp.console.services.dynaconfig.DynaConfigServiceActivator;
import com.accor.wcp.console.services.dynaconfig.dynamo.DynaConfigDocumentService;
import com.accor.wcp.console.services.dynaconfig.dynamo.domain.DynaConfigDocument;
import com.accor.wcp.console.services.dynaconfig.dynamo.domain.DynaConfigDocument.PropertyDocument;
import com.accor.wcp.console.services.dynaconfig.instance.communication.InstanceRequestHandler;
import com.accor.wcp.console.services.dynaconfig.instance.communication.dto.DynaConfigRequest;
import com.accor.wcp.console.services.dynaconfig.instance.domain.DynaConfigInstance;
import com.accor.wcp.sdk.service.dynaconfig.ServiceDefinition;
import jakarta.annotation.PostConstruct;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class SynchronizeInstanceService implements ManagedInstanceListener {
  private final DynaConfigInstanceManager dynaConfigInstanceManager;
  private final InstanceRequestHandler instanceRequestHandler;
  private final DynaConfigDocumentService documentService;
  private final ApplicationInstanceManager applicationInstanceManager;
  private final WCPConsoleUserNotificationService consoleUserNotificationService;

  public SynchronizeInstanceService(
      InstanceRequestHandler instanceRequestHandler,
      DynaConfigDocumentService documentService,
      DynaConfigInstanceManager dynaConfigInstanceManager,
      ApplicationInstanceManager applicationInstanceManager,
      WCPConsoleUserNotificationService consoleUserNotificationService) {
    this.applicationInstanceManager = applicationInstanceManager;
    this.instanceRequestHandler = instanceRequestHandler;
    this.dynaConfigInstanceManager = dynaConfigInstanceManager;
    this.documentService = documentService;
    this.consoleUserNotificationService = consoleUserNotificationService;
  }

  public String updateInstancesProperty(
      String domain, String env, String app, String propKey, String propValue) {
    if (Objects.isNull(propKey)) {
      return null;
    }
    return instanceRequestHandler.broadcastUpdateRequest(domain, env, app, propKey, propValue);
  }

  public String updateInstancesProperties(
      String domain, String env, String app, Map<String, String> updatedProperties) {
    if (CollectionUtils.isEmpty(updatedProperties)) {
      return null;
    }
    return instanceRequestHandler.broadcastMultiUpdatesRequest(domain, env, app, updatedProperties);
  }

  public String updateInstanceProperties(
      String instanceId,
      String domain,
      String env,
      String app,
      Map<String, String> updatedProperties) {
    if (Objects.isNull(updatedProperties)) {
      return null;
    }

    return instanceRequestHandler.sendMultiUpdatesRequestToInstance(
        domain, env, app, instanceId, updatedProperties);
  }

  public String refreshInstances(String domain, String env, String app) {
    this.dynaConfigInstanceManager.resetInstancesPropertiesMap(domain, env, app);
    return this.instanceRequestHandler.broadcastRefreshRequest(domain, env, app);
  }

  public List<DynaConfigRequest> getRequests(String domain, String env, String appId) {
    return instanceRequestHandler.getRequests(domain, env, appId);
  }

  public void syncInstance(
      String instanceId,
      String domain,
      String env,
      String appId,
      Set<DynaConfigInstance> instanceProps) {
    Map<String, PropertyDocument> savedProperties =
        documentService
            .getDynaConfigDocument(domain, env, appId)
            .map(DynaConfigDocument::getPropertyValues)
            .orElse(emptyMap());

    Map<String, String> instanceProperties =
        instanceProps.stream()
            .filter(this::filterNullKey)
            .collect(
                toMap(DynaConfigInstance::getPropertyKey, DynaConfigInstance::getCurrentValue));

    Map<String, String> nonSynchroProps = new HashMap<>();
    // Compare current instance properties with wcp saved/override values
    instanceProperties.forEach(
        (key, value) -> {
          if (savedProperties.containsKey(key)) {
            String savedValue = savedProperties.get(key).getValue();
            // FIXME Pb when instance properties already exist before (example restarting case)
            nonSynchroProps.put(key, savedValue);
            if (!Objects.equals(savedValue, value)) {
              log.info("Un-synchronized for instance:{}, property: {}", instanceId, key);
//              nonSynchroProps.put(key, savedValue);
            }
          } else {
            documentService.updateDocumentProperty(domain, env, appId, key, value);
          }
        });
    updateInstanceProperties(instanceId, domain, env, appId, nonSynchroProps);

    // Disabled (bad specification)
    // TODO - removing removed properties must not be done here but at manifest initialization time !
//    // remove orphans wcp saved properties => changed with log
    savedProperties.keySet().stream()
        .filter(key -> !instanceProperties.containsKey(key))
        .forEach(key -> logOrphanProperty(instanceId, domain, env, appId, key));
  }

  private boolean filterNullKey(DynaConfigInstance dynaConfigInstance) {
    if (isNull(dynaConfigInstance.getPropertyKey())) {
      log.warn("Null property in dynaConfigInstance: {}", dynaConfigInstance);
      return false;
    }
    return true;
  }

  private void logOrphanProperty(String instanceId, String domain, String env, String appId, String key) {
    UserNotification notification = DefaultUserNotification.builder()
            .serviceId(SERVICE_ID)
            .applicationId(appId)
            .date(new Date())
            .level(NotificationLevelType.INFO)
            .domain(domain)
            .env(env)
            .description(String.format("Orphan property: %s found from instanceId: %s", key, instanceId))
            .build();
//    String notificationId = consoleUserNotificationService.create(SERVICE_ID, notification);
    String notificationId = "TODO sync notification issue";
    log.warn("Seams there is orphan property {} from instance: {}, domain={}, env={}, appId={} (notificationId={})",
            key, instanceId, domain, env, appId, notificationId);
  }

  @PostConstruct
  void init() {
    applicationInstanceManager.addListener(this);
  }

  @Override
  public void onNewInstance(ManagedInstanceEvent event) {
//    log.info("dynaConfigInstances found for instance: {}", event);

//    instanceRequestHandler.sendRefreshRequestToInstance(
//        event.getDomain(),
//        event.getEnv(),
//        event.getApplicationId(),
//        event.getApplicationInstance());
  }

  @Override
  public void onRemoveInstance(ManagedInstanceEvent event) {
    log.info("dynaConfigInstances stopped instance: {}", event);

    dynaConfigInstanceManager.resetInstancesPropertiesMap(
        event.getDomain(),
        event.getEnv(),
        event.getApplicationId(),
        event.getApplicationInstance());
  }
}
