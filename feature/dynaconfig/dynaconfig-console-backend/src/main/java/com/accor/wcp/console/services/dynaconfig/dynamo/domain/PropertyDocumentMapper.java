package com.accor.wcp.console.services.dynaconfig.dynamo.domain;

import com.accor.wcp.console.services.dynaconfig.dto.PropertyDto;
import com.accor.wcp.console.services.dynaconfig.dynamo.domain.DynaConfigDocument.PropertyDocument;
import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PropertyDocumentMapper {
  List<PropertyDocument> toPropertyDocuments(Collection<PropertyDto> propertyDto);

  PropertyDocument toPropertyDocument(PropertyDto propertyDto);
}
