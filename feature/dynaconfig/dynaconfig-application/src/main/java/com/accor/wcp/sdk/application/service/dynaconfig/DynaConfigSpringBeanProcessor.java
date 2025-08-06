package com.accor.wcp.sdk.application.service.dynaconfig;

import static com.accor.wcp.sdk.service.dynaconfig.ServiceDefinition.SERVICE_ID;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.beans.PropertyAccessorFactory.forDirectFieldAccess;

import com.accor.wcp.sdk.application.communication.App2WCPConsoleCommunicationService;
import com.accor.wcp.sdk.application.service.dynaconfig.configproperties.ConfigurationPropertiesFieldsBrowser;
import com.accor.wcp.sdk.application.service.dynaconfig.configproperties.FoundField;
import com.accor.wcp.sdk.service.dynaconfig.communication.InstanceInitPropertiesMessageDto;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;

@Slf4j
@RequiredArgsConstructor
class DynaConfigSpringBeanProcessor {
  private final ApplicationContext applicationContext;

  private final App2WCPConsoleCommunicationService app2WCPConsoleCommunicationService;

  private final DynaConfigPropertiesServiceImpl dynaConfigPropertiesService;

  private Map<String, List<DynaConfigTargetMetadata>> targetMetadataByPropertyName;

  Map<String, List<DynaConfigTargetMetadata>> getTargetMetadataByPropertyName() {
    return targetMetadataByPropertyName;
  }

  @SneakyThrows
  void sendInitInstancePropertiesMessage() {
    targetMetadataByPropertyName =
        applicationContext.getBeansWithAnnotation(DynaConfigEnabler.class).values().stream()
            .map(this::computeDynaConfigTargetMetadata)
            .flatMap(Collection::stream)
            .collect(Collectors.groupingBy(DynaConfigTargetMetadata::getPropertyName));
    log.info("targetMetadataByPropertyName: {}", targetMetadataByPropertyName);

    Map<String, Set<Object>> initialPropertyValues = computeInitialPropertyValues();

    // Save current properties
    dynaConfigPropertiesService.initializeWithLocalPropertiesValues(initialPropertyValues);

    // Sending init message to platform
    InstanceInitPropertiesMessageDto instanceInitPropertiesMessageDto =
        InstanceInitPropertiesMessageDto.builder().propertyValues(initialPropertyValues).build();
    app2WCPConsoleCommunicationService.notify(SERVICE_ID, instanceInitPropertiesMessageDto);
  }

  private Map<String, Set<Object>> computeInitialPropertyValues() {
    return targetMetadataByPropertyName.entrySet().stream()
        .map(this::flatPropertyValue)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private AbstractMap.SimpleEntry<String, Set<Object>> flatPropertyValue(
      Map.Entry<String, List<DynaConfigTargetMetadata>> e) {
    String key = e.getKey();
    Set<Object> values =
        e.getValue().stream()
            .map(DynaConfigTargetMetadata::getCurrentValue)
            .collect(Collectors.toSet());
    return new AbstractMap.SimpleEntry<>(key, values);
  }

  List<DynaConfigTargetMetadata> computeDynaConfigTargetMetadata(Object o) {
    // Special case for ConfigurationProperties
    ConfigurationProperties anno = o.getClass().getAnnotation(ConfigurationProperties.class);
    if (nonNull(anno)) {
      return ConfigurationPropertiesFieldsBrowser.readDynaConfigAttributeOn(o).stream()
          .map(this::mapToDynaConfigTargetMetadata)
          .toList();
    }

    // Else be careful with @Configuration => proxy
    Class<?> targetClass;
    if (AopUtils.isAopProxy(o)) {
      targetClass = ClassUtils.getUserClass(o.getClass());
    } else {
      targetClass = o.getClass();
    }
    return Arrays.stream(targetClass.getDeclaredFields())
        .filter(this::fieldIsAnnotatedWith)
        .map(field -> this.dynaConfigTargetMetadata(o, field))
        .toList();
  }

  private DynaConfigTargetMetadata mapToDynaConfigTargetMetadata(FoundField foundField) {
    Field field = foundField.getField();
    DynaConfigAttribute dynaConfigAttribute =
        field.getDeclaredAnnotation(DynaConfigAttribute.class);
    return DynaConfigTargetMetadata.builder()
        .propertyName(foundField.getPropertyName())
        .targetObject(foundField.getTargetObject())
        .field(field)
        .beanPropertyAccess(dynaConfigAttribute.beanPropertyAccess())
        .initialValue(foundField.getValue())
        .currentValue(foundField.getValue())
        .build();
  }

  private DynaConfigTargetMetadata dynaConfigTargetMetadata(Object targetObject, Field field) {
    DynaConfigAttribute dynaConfigAttribute =
        field.getDeclaredAnnotation(DynaConfigAttribute.class);

    String propertyName = dynaConfigAttribute.value();
    // for backward compatibility
    if (isEmpty(propertyName)) {
      propertyName = dynaConfigAttribute.propertyName();
    }
    if (isEmpty(propertyName)) {
      String valuePropertyName = computeValueAnnotationPropertyName(field);

      if (nonNull(valuePropertyName)) {
        propertyName = valuePropertyName;
      } else {
        propertyName = field.getName();
        log.info(
            "No dynamic config property name defined for target class: {}, using default field name: {}",
            targetObject.getClass().getName(),
            propertyName);
      }
    }

    Object initialValue = null;
    try {
      initialValue = forDirectFieldAccess(targetObject).getPropertyValue(field.getName());
    } catch (Exception e) {
      log.warn(
          "Error getting initial value for field: {} of target object: {}",
          field.getName(),
          targetObject);
    }

    return DynaConfigTargetMetadata.builder()
        .propertyName(propertyName)
        .targetObject(targetObject)
        .field(field)
        .beanPropertyAccess(dynaConfigAttribute.beanPropertyAccess())
        .initialValue(initialValue)
        .currentValue(initialValue)
        .build();
  }

  static String computeValueAnnotationPropertyName(Field field) {
    String valuePropertyName = null;
    Value springValueAnnotation = field.getAnnotation(Value.class);
    if (nonNull(springValueAnnotation)) {
      String valueExpression = springValueAnnotation.value();
      if (valueExpression.startsWith("${")) {
        valuePropertyName =
            valueExpression.substring(2, valueExpression.length() - 1).split(":")[0];
      }
    }
    return valuePropertyName;
  }

  private boolean fieldIsAnnotatedWith(Field field) {
    return field.isAnnotationPresent(DynaConfigAttribute.class);
  }
}
