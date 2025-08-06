package com.accor.wcp.console.services.featureflipping;

import com.accor.wcp.console.services.featureflipping.dto.FeatureFlippingDto;
import com.accor.wcp.console.services.featureflipping.dto.FeatureFlippingKeyDto;
import com.accor.wcp.console.services.featureflipping.dto.PropertyDto;
import com.accor.wcp.console.services.featureflipping.dto.Source;
import com.accor.wcp.console.services.featureflipping.dto.mapper.FeatureFlippingDtoMapper;
import com.accor.wcp.console.services.featureflipping.dynamo.FeatureFlippingDocumentService;
import com.accor.wcp.console.services.featureflipping.dynamo.domain.FeatureFlippingDocument.PropertyDocument;
import com.accor.wcp.console.services.featureflipping.instance.FeatureFlippingInstanceManager;
import com.accor.wcp.console.services.featureflipping.instance.FeatureFlippingSynchronizeInstanceService;
import com.accor.wcp.console.services.featureflipping.instance.communication.dto.FeatureFlippingRequest;
import com.accor.wcp.console.services.featureflipping.instance.domain.FeatureFlippingInstance;
import com.accor.wcp.console.services.featureflipping.manifest.FeatureFlippingPropertySettings;
import com.accor.wcp.console.services.featureflipping.manifest.FeatureFlippingSettingsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

@Service
@RequiredArgsConstructor
public class FeatureFlippingService {

  private final FeatureFlippingServiceActivator serviceActivator;
  private final FeatureFlippingDocumentService documentService;
  private final FeatureFlippingInstanceManager instanceManager;
  private final FeatureFlippingSynchronizeInstanceService featureFlippingSynchronizeInstanceService;

  private final FeatureFlippingDtoMapper featureFlippingDtoMapper;

  public List<String> refresh(String domain, String env, String applicationId) {
    return singletonList(
        this.featureFlippingSynchronizeInstanceService.refreshInstances(domain, env, applicationId));
  }

  public List<String> update(String domain, String env, String appId, String key, String value) {
    documentService.updateDocumentProperty(domain, env, appId, key, value);

    return singletonList(
        featureFlippingSynchronizeInstanceService.updateInstancesProperty(domain, env, appId, key, value));
  }

  public List<String> update(String domain, String env, String appId, FeatureFlippingDto inputConfig, String username) {
    List<PropertyDto> inputPropertiesDtos = getUpdatableProperties(inputConfig);
    documentService.updateDocumentProperties(domain, env, appId, inputPropertiesDtos, username);

    Map<String, String> inputProperties =
        inputPropertiesDtos.stream()
            .collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);
    return singletonList(
        featureFlippingSynchronizeInstanceService.updateInstancesProperties(domain, env, appId, inputProperties));
  }

  public FeatureFlippingDto getByApplicationId(String domain, String env, String applicationId) {
    String id = domain + "-" + env + "-" + applicationId;
    Map<String, FeatureFlippingSettingsDto> dynaConfigManifestsById =
        serviceActivator.getServiceManifest().getFeatureFlippingSettingsMap();
    if (!dynaConfigManifestsById.containsKey(id)) {
      return null;
    }

    List<FeatureFlippingPropertySettings> manifestProperties =
        dynaConfigManifestsById.get(id).getKeys();
    List<PropertyDocument> savedProperties =
        documentService.getFeatureFlippingProperties(domain, env, applicationId);
    List<FeatureFlippingInstance> instancesProperties =
        instanceManager.getInstancesProperties(domain, env, applicationId);
    List<FeatureFlippingRequest> requests =
        featureFlippingSynchronizeInstanceService.getRequests(domain, env, applicationId);

    return featureFlippingDtoMapper.mapFeatureFlippingConfigDto(
        manifestProperties, savedProperties, instancesProperties, requests);
  }

  private List<PropertyDto> getUpdatableProperties(FeatureFlippingDto configDto) {
    return configDto.getProperties().values().stream()
        .filter(p -> !Source.APP.equals(p.getSource()))
        .toList();
  }

  public void flush(String domain, String env, String applicationId) {
      documentService.flushHistory(domain, env, applicationId);
  }

  List<FeatureFlippingKeyDto> list(String domain, String env, String appId) {
    String name = domain + "-" + env + "-" + appId;
    FeatureFlippingSettingsDto featureFlippingSettingsDto = serviceActivator.getServiceManifest().getFeatureFlippingSettingsMap().get(name);
    if (featureFlippingSettingsDto == null) {
      return Collections.emptyList();
    }
    return featureFlippingSettingsDto
            .getKeys().stream().map(prop -> FeatureFlippingKeyDto.builder()
                    .key(prop.getKey())
                    .expiration(prop.getExpiration())
                    .appId(appId)
                    .build()).toList();
  }
}
