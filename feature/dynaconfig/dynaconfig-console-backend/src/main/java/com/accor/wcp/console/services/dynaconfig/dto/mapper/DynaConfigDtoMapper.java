package com.accor.wcp.console.services.dynaconfig.dto.mapper;

import com.accor.wcp.console.services.dynaconfig.dto.DynaConfigDto;
import com.accor.wcp.console.services.dynaconfig.dto.PropertyDto;
import com.accor.wcp.console.services.dynaconfig.dynamo.domain.DynaConfigDocument.PropertyDocument;
import com.accor.wcp.console.services.dynaconfig.instance.communication.dto.DynaConfigRequest;
import com.accor.wcp.console.services.dynaconfig.instance.domain.DynaConfigInstance;
import com.accor.wcp.console.services.dynaconfig.manifest.DynaConfigPropertySettings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DynaConfigDtoMapper {

  private final PropertyDtoMapper propertyDtoMapper;
  private final InstancePropertyDtoMapper instancePropertyDtoMapper;
  private final PropertyChangesDtoMapper propertyChangesDtoMapper;
  private final IssuedCommandsDtoMapper issuedCommandsDtoMapper;

  public DynaConfigDto mapDynaConfigDto(
      List<DynaConfigPropertySettings> manifestProperties,
      List<PropertyDocument> documentProperties,
      List<DynaConfigInstance> instancesProperties,
      List<DynaConfigRequest> requests) {

    return DynaConfigDto.builder()
        .properties(mapProperties(manifestProperties, documentProperties, instancesProperties))
        .changes(propertyChangesDtoMapper.mapAllChanges(documentProperties))
        .issuedCommands(issuedCommandsDtoMapper.mapAllCommands(requests))
        .build();
  }

  private Map<String, PropertyDto> mapProperties(
      List<DynaConfigPropertySettings> manifestProperties,
      List<PropertyDocument> documentProperties,
      List<DynaConfigInstance> instancesProperties) {
    Map<String, PropertyDto> properties = new HashMap<>();

    for (PropertyDto dto : propertyDtoMapper.mapManifestProperties(manifestProperties)) {
      Optional<PropertyDocument> document = getPropertyDocumentByKey(dto.getName(), documentProperties);
      if (document.isPresent()) {
        dto.setValue(document.get().getValue());
        dto.setComment(document.get().getComment());
      }
      properties.put(dto.getName(), dto);
    }

    instancePropertyDtoMapper.updatePropertiesWithInstancesInfo(properties, instancesProperties);
    instancePropertyDtoMapper.setPropertiesStatus(properties);
    instancePropertyDtoMapper.setPropertiesValue(properties);

    return properties;
  }

  private Optional<PropertyDocument> getPropertyDocumentByKey(
      String key, List<PropertyDocument> documents) {
    return documents.stream().filter(document -> key.equals(document.getName())).findFirst();
  }
}
