package com.accor.wcp.console.services.featureflipping.dto.mapper;

import static com.accor.wcp.console.services.featureflipping.dto.Status.NOT_SYNCHRO;
import static com.accor.wcp.console.services.featureflipping.dto.Status.SYNCHRO;

import com.accor.wcp.console.services.featureflipping.dto.PropertyDto;
import com.accor.wcp.console.services.featureflipping.dto.PropertyInstanceDto;
import com.accor.wcp.console.services.featureflipping.instance.domain.FeatureFlippingInstance;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.util.CollectionUtils;

@Mapper(componentModel = "spring")
public interface FeatureFlippingInstancePropertyDtoMapper {

  @Mapping(target = "source", constant = "APP")
  PropertyDto mapInstanceProperty(String key, List<PropertyInstanceDto> instances);

  PropertyInstanceDto mapInstance(FeatureFlippingInstance propertyDocument);

  default void updatePropertiesWithInstancesInfo(
      Map<String, PropertyDto> properties, List<FeatureFlippingInstance> instancesProperties) {

    for (FeatureFlippingInstance instanceProperty : instancesProperties) {
      PropertyInstanceDto instanceDto = this.mapInstance(instanceProperty);
      linkInstanceToPropertyDto(properties, instanceProperty.getPropertyKey(), instanceDto);
    }
  }

  default void linkInstanceToPropertyDto(
      Map<String, PropertyDto> properties, String propertyKey, PropertyInstanceDto instance) {

    List<PropertyInstanceDto> instances = new ArrayList<>();
    instances.add(instance);

    if (properties.containsKey(propertyKey)) {
      PropertyDto propertyDto = properties.get(propertyKey);

      if (!CollectionUtils.isEmpty(propertyDto.getInstances())) {
        instances.addAll(propertyDto.getInstances());
      }
      propertyDto.setInstances(instances);
    } else {
      properties.put(propertyKey, mapInstanceProperty(propertyKey, instances));
    }
  }

  default void setPropertiesStatus(Map<String, PropertyDto> properties) {
    for (PropertyDto property : properties.values()) {
      if (!CollectionUtils.isEmpty(property.getInstances())) {
        setInstanceStatus(property.getValue(), property.getInstances());
      }

      if (CollectionUtils.isEmpty(property.getInstances()) || !isAllInstancesSynchro(property.getInstances())) {
        property.setStatus(NOT_SYNCHRO);
      } else {
        property.setStatus(SYNCHRO);
      }
    }
  }

  default void setPropertiesValue(Map<String, PropertyDto> properties) {
    for (PropertyDto property : properties.values()) {
      if (Objects.isNull(property.getValue())
          && !CollectionUtils.isEmpty(property.getInstances())
          && SYNCHRO.equals(property.getStatus())) {
        property.setValue(property.getInstances().get(0).getCurrentValue());
      }
    }
  }

  default void setInstanceStatus(String propertyValue, List<PropertyInstanceDto> instances) {
    for (PropertyInstanceDto instance : instances) {
      if (Objects.isNull(propertyValue) || Objects.equals(propertyValue, instance.getCurrentValue())) {
        instance.setStatus(SYNCHRO);
      } else {
        instance.setStatus(NOT_SYNCHRO);
      }
    }
  }
  default boolean isAllInstancesSynchro(List<PropertyInstanceDto> instances) {
    return instances.stream().allMatch(i -> SYNCHRO.equals(i.getStatus()));
  }
}
