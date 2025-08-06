package com.accor.wcp.console.services.featureflipping.dto.mapper;

import com.accor.wcp.console.services.featureflipping.dto.PropertyChangeDto;
import com.accor.wcp.console.services.featureflipping.dynamo.domain.FeatureFlippingDocument.PropertyDocument;
import com.accor.wcp.console.services.featureflipping.dynamo.domain.FeatureFlippingDocument.PropertyHistoryDocument;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.mapstruct.Mapper;
import org.springframework.util.CollectionUtils;

@Mapper(componentModel = "spring")
public interface FeatureFlippingPropertyChangesDtoMapper {

  default List<PropertyChangeDto> mapAllChanges(List<PropertyDocument> documents) {
    if (CollectionUtils.isEmpty(documents)) {
      return Collections.emptyList();
    }

    return documents.stream()
        .map(this::mapPropertyChanges)
        .flatMap(Collection::stream)
        .sorted(Comparator.comparingLong(PropertyChangeDto::getTimestamp).reversed())
        .toList();
  }

  default List<PropertyChangeDto> mapPropertyChanges(PropertyDocument property) {
    if (CollectionUtils.isEmpty(property.getHistory())) {
      return Collections.emptyList();
    }

    return property.getHistory().stream()
        .map(change -> mapPropertyChange(property.getKey(), change))
        .toList();
  }

  PropertyChangeDto mapPropertyChange(
      String propertyName, PropertyHistoryDocument historyDocument);
}
