package com.accor.wcp.console.services.dynaconfig;

import static java.util.Collections.singletonList;

import com.accor.wcp.console.services.dynaconfig.dto.DynaConfigDto;
import com.accor.wcp.console.services.dynaconfig.dto.PropertyDto;
import com.accor.wcp.console.services.dynaconfig.dto.Source;
import com.accor.wcp.console.services.dynaconfig.dto.mapper.DynaConfigDtoMapper;
import com.accor.wcp.console.services.dynaconfig.dynamo.DynaConfigDocumentService;
import com.accor.wcp.console.services.dynaconfig.dynamo.domain.DynaConfigDocument.PropertyDocument;
import com.accor.wcp.console.services.dynaconfig.instance.DynaConfigInstanceManager;
import com.accor.wcp.console.services.dynaconfig.instance.SynchronizeInstanceService;
import com.accor.wcp.console.services.dynaconfig.instance.communication.dto.DynaConfigRequest;
import com.accor.wcp.console.services.dynaconfig.instance.domain.DynaConfigInstance;
import com.accor.wcp.console.services.dynaconfig.manifest.DynaConfigPropertySettings;
import com.accor.wcp.console.services.dynaconfig.manifest.DynaConfigSettingsDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DynaConfigService {

  private final DynaConfigServiceActivator serviceActivator;
  private final DynaConfigDocumentService documentService;
  private final DynaConfigInstanceManager instanceManager;
  private final SynchronizeInstanceService synchronizeInstanceService;

  private final DynaConfigDtoMapper dynaConfigDtoMapper;

  public List<String> refresh(String domain, String env, String applicationId) {
    return singletonList(
        this.synchronizeInstanceService.refreshInstances(domain, env, applicationId));
  }

  public List<String> update(String domain, String env, String appId, String key, String value) {
    documentService.updateDocumentProperty(domain, env, appId, key, value);

    return singletonList(
        synchronizeInstanceService.updateInstancesProperty(domain, env, appId, key, value));
  }

  public List<String> update(String domain, String env, String appId, DynaConfigDto inputConfig, String username) {
    List<PropertyDto> inputPropertiesDtos = getUpdatableProperties(inputConfig);
    documentService.updateDocumentProperties(domain, env, appId, inputPropertiesDtos, username);

    Map<String, String> inputProperties =
        inputPropertiesDtos.stream()
            .collect(HashMap::new, (m, v) -> m.put(v.getName(), v.getValue()), HashMap::putAll);
    return singletonList(
        synchronizeInstanceService.updateInstancesProperties(domain, env, appId, inputProperties));
  }

  public DynaConfigDto getByApplicationId(String domain, String env, String applicationId) {
    String id = domain + "-" + env + "-" + applicationId;
    Map<String, DynaConfigSettingsDto> dynaConfigManifestsById =
        serviceActivator.getServiceManifest().getDynaConfigSettingsMap();
    if (!dynaConfigManifestsById.containsKey(id)) {
      return null;
    }

    List<DynaConfigPropertySettings> manifestProperties =
        dynaConfigManifestsById.get(id).getProperties();
    List<PropertyDocument> savedProperties =
        documentService.getDynaConfigProperties(domain, env, applicationId);
    List<DynaConfigInstance> instancesProperties =
        instanceManager.getInstancesProperties(domain, env, applicationId);
    List<DynaConfigRequest> requests =
        synchronizeInstanceService.getRequests(domain, env, applicationId);

    return dynaConfigDtoMapper.mapDynaConfigDto(
        manifestProperties, savedProperties, instancesProperties, requests);
  }

  private List<PropertyDto> getUpdatableProperties(DynaConfigDto configDto) {
    return configDto.getProperties().values().stream()
        .filter(p -> !Source.APP.equals(p.getSource()))
        .toList();
  }

  public void flush(String domain, String env, String applicationId) {
      documentService.flushHistory(domain, env, applicationId);
  }
}
