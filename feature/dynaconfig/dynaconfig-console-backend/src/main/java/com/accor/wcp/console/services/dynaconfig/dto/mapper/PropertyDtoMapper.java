package com.accor.wcp.console.services.dynaconfig.dto.mapper;

import com.accor.wcp.console.services.dynaconfig.dto.PropertyDto;
import com.accor.wcp.console.services.dynaconfig.manifest.DynaConfigPropertySettings;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PropertyDtoMapper {

  List<PropertyDto> mapManifestProperties(List<DynaConfigPropertySettings> manifestSettings);

  @Mapping(target = "source", constant = "MANIFEST")
  PropertyDto mapManifestProperty(DynaConfigPropertySettings manifestSetting);

}
