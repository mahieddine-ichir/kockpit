package com.accor.wcp.console.services.featureflipping.dynamo.domain;

import com.accor.wcp.console.services.featureflipping.dto.PropertyDto;
import com.accor.wcp.console.services.featureflipping.dynamo.domain.FeatureFlippingDocument.PropertyDocument;
import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FeatureFlippingPropertyDocumentMapper {
  List<PropertyDocument> toPropertyDocuments(Collection<PropertyDto> propertyDto);

  PropertyDocument toPropertyDocument(PropertyDto propertyDto);
}
