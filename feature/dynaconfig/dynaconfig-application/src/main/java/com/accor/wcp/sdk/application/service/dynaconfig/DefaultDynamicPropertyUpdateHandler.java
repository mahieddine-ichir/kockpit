package com.accor.wcp.sdk.application.service.dynaconfig;

import static org.springframework.beans.PropertyAccessorFactory.forBeanPropertyAccess;
import static org.springframework.beans.PropertyAccessorFactory.forDirectFieldAccess;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.core.convert.ConversionService;

@Slf4j
@RequiredArgsConstructor
class DefaultDynamicPropertyUpdateHandler implements DynamicPropertyUpdateHandler {
  private final DynaConfigSpringBeanProcessor dynaConfigSpringBeanProcessor;
  private final ConversionService conversionService;

  @Override
  public void update(String propertyName, String newValue) {
    List<DynaConfigTargetMetadata> dynaConfigTargetMetadata =
        dynaConfigSpringBeanProcessor.getTargetMetadataByPropertyName().get(propertyName);
    if (isEmpty(dynaConfigTargetMetadata)) {
      log.warn(
          "No handler defined to update property : {} with new value: {}", propertyName, newValue);
      throw new InvalidParameterException(
          "No handler defined to update property : "
              + propertyName
              + " with new value: "
              + newValue);
    }
    List<Exception> exceptions =
        dynaConfigTargetMetadata.stream()
            .map(metadata -> update(newValue, metadata))
            .filter(Objects::nonNull)
            .toList();
    // All ok
    if (exceptions.isEmpty()) {
      return;
    }
    // Else throw exceptions
    throw new IllegalStateException(
        "Exceptions when updating property values. List: " + exceptions);
  }

  private Exception update(String newValue, DynaConfigTargetMetadata metadata) {
    Object converted;
    try {
      converted = conversionService.convert(newValue, metadata.getField().getType());
    } catch (Exception e) {
      log.warn(
          "Error converting newValue: {} to target type: {}. Skipping property value update for: {}",
          newValue,
          metadata.getField().getType(),
          metadata.getTargetObject());
      return e;
    }
    ConfigurablePropertyAccessor propertyAccessor;
    if (metadata.isBeanPropertyAccess()) {
      propertyAccessor = forBeanPropertyAccess(metadata.getTargetObject());
    } else {
      propertyAccessor = forDirectFieldAccess(metadata.getTargetObject());
    }
    try {
      propertyAccessor.setPropertyValue(metadata.getField().getName(), converted);
      metadata.setCurrentValue(converted);
    } catch (Exception e) {
      log.warn(
          "Error setting newValue: {} to target type: {}. Skipping property value update for: {}",
          newValue,
          metadata.getField().getType(),
          metadata.getTargetObject());
      return e;
    }
    return null;
  }
}
