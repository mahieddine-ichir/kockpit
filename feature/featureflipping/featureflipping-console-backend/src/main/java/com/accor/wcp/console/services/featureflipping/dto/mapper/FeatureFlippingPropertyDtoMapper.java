package com.accor.wcp.console.services.featureflipping.dto.mapper;

import com.accor.wcp.console.services.featureflipping.dto.PropertyDto;
import com.accor.wcp.console.services.featureflipping.manifest.FeatureFlippingPropertySettings;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeatureFlippingPropertyDtoMapper {

  List<PropertyDto> mapManifestProperties(List<FeatureFlippingPropertySettings> manifestSettings);

  @Mapping(target = "source", constant = "MANIFEST")
  PropertyDto mapManifestProperty(FeatureFlippingPropertySettings manifestSetting);

}
