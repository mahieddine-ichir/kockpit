package com.accor.wcp.console.services.featureflipping.dto.mapper;

import com.accor.wcp.console.services.featureflipping.dto.FeatureFlippingDto;
import com.accor.wcp.console.services.featureflipping.dto.PropertyDto;
import com.accor.wcp.console.services.featureflipping.dynamo.domain.FeatureFlippingDocument.PropertyDocument;
import com.accor.wcp.console.services.featureflipping.instance.communication.dto.FeatureFlippingRequest;
import com.accor.wcp.console.services.featureflipping.instance.domain.FeatureFlippingInstance;
import com.accor.wcp.console.services.featureflipping.manifest.FeatureFlippingPropertySettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FeatureFlippingDtoMapper {

  private final FeatureFlippingPropertyDtoMapper featureFlippingPropertyDtoMapper;
  private final FeatureFlippingInstancePropertyDtoMapper featureFlippingInstancePropertyDtoMapper;
  private final FeatureFlippingPropertyChangesDtoMapper featureFlippingPropertyChangesDtoMapper;
  private final FeatureFlippingIssuedCommandsDtoMapper featureFlippingIssuedCommandsDtoMapper;

  public FeatureFlippingDto mapFeatureFlippingConfigDto(
      List<FeatureFlippingPropertySettings> manifestProperties,
      List<PropertyDocument> documentProperties,
      List<FeatureFlippingInstance> instancesProperties,
      List<FeatureFlippingRequest> requests) {

    return FeatureFlippingDto.builder()
        .properties(mapProperties(manifestProperties, documentProperties, instancesProperties))
        .changes(featureFlippingPropertyChangesDtoMapper.mapAllChanges(documentProperties))
        .issuedCommands(featureFlippingIssuedCommandsDtoMapper.mapAllCommands(requests))
        .build();
  }

  private Map<String, PropertyDto> mapProperties(
      List<FeatureFlippingPropertySettings> manifestProperties,
      List<PropertyDocument> documentProperties,
      List<FeatureFlippingInstance> instancesProperties) {
    Map<String, PropertyDto> properties = new HashMap<>();

    List<PropertyDto> propertyDtos = featureFlippingPropertyDtoMapper.mapManifestProperties(manifestProperties);
    if (!CollectionUtils.isEmpty(propertyDtos)) {
      for (PropertyDto dto : propertyDtos) {
        Optional<PropertyDocument> document = getPropertyDocumentByKey(dto.getKey(), documentProperties);
        if (document.isPresent()) {
          dto.setValue(document.get().getValue());
          dto.setComment(document.get().getComment());
          dto.setExpiration(document.get().getExpiration());
        }
        properties.put(dto.getKey(), dto);
      }
    }

    featureFlippingInstancePropertyDtoMapper.updatePropertiesWithInstancesInfo(properties, instancesProperties);
    featureFlippingInstancePropertyDtoMapper.setPropertiesStatus(properties);
    featureFlippingInstancePropertyDtoMapper.setPropertiesValue(properties);

    return properties;
  }

  private Optional<PropertyDocument> getPropertyDocumentByKey(
      String key, List<PropertyDocument> documents) {
    return documents.stream().filter(document -> key.equals(document.getKey())).findFirst();
  }
}
