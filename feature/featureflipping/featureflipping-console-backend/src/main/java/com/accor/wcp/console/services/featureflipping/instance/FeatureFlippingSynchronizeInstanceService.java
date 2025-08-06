package com.accor.wcp.console.services.featureflipping.instance;

import static java.util.Collections.emptyMap;
import com.accor.wcp.console.sdk.topology.ApplicationInstanceManager;
import com.accor.wcp.console.sdk.topology.ManagedInstanceEvent;
import com.accor.wcp.console.sdk.topology.ManagedInstanceListener;
import com.accor.wcp.console.services.featureflipping.dynamo.FeatureFlippingDocumentService;
import com.accor.wcp.console.services.featureflipping.dynamo.domain.FeatureFlippingDocument;
import com.accor.wcp.console.services.featureflipping.dynamo.domain.FeatureFlippingDocument.PropertyDocument;
import com.accor.wcp.console.services.featureflipping.instance.communication.FeatureFlippingInstanceRequestHandler;
import com.accor.wcp.console.services.featureflipping.instance.communication.dto.FeatureFlippingRequest;
import com.accor.wcp.console.services.featureflipping.instance.domain.FeatureFlippingInstance;
import jakarta.annotation.PostConstruct;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class FeatureFlippingSynchronizeInstanceService implements ManagedInstanceListener {
  private final FeatureFlippingInstanceManager featureFlippingInstanceManager;
  private final FeatureFlippingInstanceRequestHandler featureFlippingInstanceRequestHandler;
  private final FeatureFlippingDocumentService documentService;
  private final ApplicationInstanceManager applicationInstanceManager;

  public FeatureFlippingSynchronizeInstanceService(
      FeatureFlippingInstanceRequestHandler featureFlippingInstanceRequestHandler,
      FeatureFlippingDocumentService documentService,
      FeatureFlippingInstanceManager featureFlippingInstanceManager,
      ApplicationInstanceManager applicationInstanceManager) {
    this.applicationInstanceManager = applicationInstanceManager;
    this.featureFlippingInstanceRequestHandler = featureFlippingInstanceRequestHandler;
    this.featureFlippingInstanceManager = featureFlippingInstanceManager;
    this.documentService = documentService;
  }

  public String updateInstancesProperty(
      String domain, String env, String app, String propKey, String propValue) {
    if (Objects.isNull(propKey)) {
      return null;
    }
    return featureFlippingInstanceRequestHandler.broadcastUpdateRequest(domain, env, app, propKey, propValue);
  }

  public String updateInstancesProperties(
      String domain, String env, String app, Map<String, String> updatedProperties) {
    if (CollectionUtils.isEmpty(updatedProperties)) {
      return null;
    }
    return featureFlippingInstanceRequestHandler.broadcastMultiUpdatesRequest(domain, env, app, updatedProperties);
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

    return featureFlippingInstanceRequestHandler.sendMultiUpdatesRequestToInstance(
        domain, env, app, instanceId, updatedProperties);
  }

  public String refreshInstances(String domain, String env, String app) {
    this.featureFlippingInstanceManager.resetInstancesPropertiesMap(domain, env, app);
    return this.featureFlippingInstanceRequestHandler.broadcastRefreshRequest(domain, env, app);
  }

  public List<FeatureFlippingRequest> getRequests(String domain, String env, String appId) {
    return featureFlippingInstanceRequestHandler.getRequests(domain, env, appId);
  }

  public void syncInstance(
      String instanceId,
      String domain,
      String env,
      String appId,
      Set<FeatureFlippingInstance> instanceProps) {
    Map<String, PropertyDocument> savedProperties =
        documentService
            .getFeatureFlippingDocument(domain, env, appId)
            .map(FeatureFlippingDocument::getPropertyValues)
            .orElse(emptyMap());

    Map<String, String> nonSynchroProps = new HashMap<>();
    savedProperties.forEach((s,propertyDocument) -> nonSynchroProps.put(propertyDocument.getKey(), propertyDocument.getValue()));
    updateInstanceProperties(instanceId, domain, env, appId, nonSynchroProps);
  }

  @PostConstruct
  void init() {
    applicationInstanceManager.addListener(this);
  }

  @Override
  public void onNewInstance(ManagedInstanceEvent event) {
//    log.info("dynaConfigInstances found for instance: {}", event);

//    featureFlippingInstanceRequestHandler.sendRefreshRequestToInstance(
//        event.getDomain(),
//        event.getEnv(),
//        event.getApplicationId(),
//        event.getApplicationInstance());
  }

  @Override
  public void onRemoveInstance(ManagedInstanceEvent event) {
    log.info("dynaConfigInstances stopped instance: {}", event);

    featureFlippingInstanceManager.resetInstancesPropertiesMap(
        event.getDomain(),
        event.getEnv(),
        event.getApplicationId(),
        event.getApplicationInstance());
  }
}
