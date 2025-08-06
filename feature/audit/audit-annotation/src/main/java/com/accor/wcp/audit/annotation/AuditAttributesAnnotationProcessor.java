package com.accor.wcp.audit.annotation;

import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

// TODO - refactor all that code!
@Slf4j
public class AuditAttributesAnnotationProcessor {

  private static Map<String, Object> parseObjectFieldsAndBuildAuditKeyValueMap(Object object)
      throws IllegalAccessException, NoSuchFieldException, NoSuchMethodException,
          InvocationTargetException, InstantiationException {
    Map<String, Object> auditParameters = new HashMap<>();
    Class<?> clazz = object.getClass();
    if (clazz.isAnnotationPresent(Audited.class)) {
      for (Field field : clazz.getDeclaredFields()) {
        if (isElementAnnotatedWithAuditAttribute(field)) {
          AuditAttribute[] auditAttributes = getAuditAttributes(field);
          Optional<Object> fieldObject = getObjectToParse(object, field);
          extractKeyValueListFromObjectField(auditAttributes, field.getName(), fieldObject)
              .forEach(entry -> auditParameters.put(entry.getKey(), entry.getValue()));
        }
      }

      // Audited method (only on root object for the moment)
      for (Method declaredMethod : clazz.getDeclaredMethods()) {
        if (isElementAnnotatedWithAuditAttribute(declaredMethod)) {
          AuditAttribute[] auditAttributes = getAuditAttributes(declaredMethod);
          Object auditedValue = null;
          try {
            auditedValue = declaredMethod.invoke(object);
          } catch (Throwable t) {
            log.info("Error getting audit value from method {}. Exception message: {}", declaredMethod.getName(), t.getMessage());
            auditedValue = "...Error..." + t.getMessage();
          }
          // Use only first annotation
          AuditAttribute auditAttribute = auditAttributes[0];
          String key = auditAttribute.key();
          if (isEmpty(key)) {
            key = declaredMethod.getName();
          }
          auditParameters.put(key, auditedValue);

          // Audit internal attributes (of result)
          auditParameters.putAll(parseObjectFieldsAndBuildAuditKeyValueMap(auditedValue));
        }
      }
    }
    return auditParameters;
  }

  public static List<Entry<String, Object>> extractKeyValueListFromObjectField(
      AuditAttribute[] auditAttributes, String fieldName, Optional<Object> fieldObject)
      throws IllegalAccessException, NoSuchFieldException, NoSuchMethodException,
          InvocationTargetException, InstantiationException {
    List<Entry<String, Object>> keyValues = new ArrayList<>();
    for (AuditAttribute auditAttribute : auditAttributes) {
      Map.Entry<String, Object> entry =
          buildMapEntryFromField(
              fieldName, fieldObject, auditAttribute.key(), auditAttribute.path());
      keyValues.add(
          Map.entry(entry.getKey(), applyFunctionToEntryValue(auditAttribute, entry.getValue())));
    }
    return keyValues;
  }

  private static Object applyFunctionToEntryValue(AuditAttribute auditAttribute, Object value)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
          InstantiationException {
    Class<? extends Function<Object, Object>> function = auditAttribute.function();
    Method method = function.getMethod("apply", Object.class);
    Constructor<? extends Function<Object, Object>> constructor = function.getConstructor();
    return method.invoke(constructor.newInstance(), value);
  }

  public static Map.Entry<String, Object> buildMapEntryFromField(
      String fieldName, Optional<Object> fieldObject, String developerDefinedKey, String path)
      throws IllegalAccessException, NoSuchFieldException {

    String key = eraseKeyWithFieldNameIfNoDevDefinedKey(fieldName, developerDefinedKey);

    String[] fieldNames = path.split("\\s*\\.\\s*");

    if (fieldObject.isPresent() && fieldObject.get() instanceof Collection<?>) {
      return buildMapEntryInCaseListOfObjects(
          fieldName, developerDefinedKey, key, fieldObject.get(), fieldNames);
    } else {
      return buildEntryInCaseOneObject(
          fieldName, developerDefinedKey, key, fieldObject, fieldNames);
    }
  }

  private static Entry<String, Object> buildEntryInCaseOneObject(
      String rootFieldName,
      String developerDefinedKey,
      String key,
      Optional<Object> localObj,
      String[] fieldNames)
      throws NoSuchFieldException, IllegalAccessException {

    for (String fieldName : fieldNames) {
      if (!isBlank(fieldName) && !fieldName.equals(rootFieldName) && localObj.isPresent()) {
        Field newField = localObj.get().getClass().getDeclaredField(fieldName);
        localObj = getObjectToParse(localObj.get(), newField);
        key = eraseKeyWithFieldNameIfNoDevDefinedKey(newField.getName(), developerDefinedKey);
      }
    }
    // TODO - improvement use a Map that accepts null value
    return Map.entry(key, localObj.orElse(""));
  }

  private static Entry<String, Object> buildMapEntryInCaseListOfObjects(
      String fieldName,
      String developerDefinedKey,
      String key,
      Object localObj,
      String[] fieldNames)
      throws NoSuchFieldException, IllegalAccessException {

    Collection<?> objects = (Collection<?>) localObj;
    List<Object> auditedValuesForField = new ArrayList<>();
    for (Object obj : objects) {
      auditedValuesForField.add(
          buildEntryInCaseOneObject(
                  fieldName, developerDefinedKey, key, Optional.of(obj), fieldNames)
              .getValue());
    }
    return Map.entry(
        key,
        auditedValuesForField.stream()
            .filter(Objects::nonNull)
            .map(Object::toString)
            .collect(joining(",")));
  }

  private static Optional<Object> getObjectToParse(Object object, Field field)
      throws IllegalAccessException {
    if (isNull(field)) {
      return Optional.empty();
    }
    field.setAccessible(true);
    if (isNull(field.get(object))) {
      return Optional.empty();
    }
    return Optional.of(field.get(object));
  }

  private static String eraseKeyWithFieldNameIfNoDevDefinedKey(
      String fieldName, String developerDefinedKey) {
    if (isBlank(developerDefinedKey)) {
      return fieldName;
    }
    return developerDefinedKey;
  }

  public static AuditAttribute[] getAuditAttributes(AnnotatedElement element) {
    AuditAttribute[] auditAttributes = null;
    if (element.isAnnotationPresent(AuditAttributes.class)) {
      auditAttributes = element.getDeclaredAnnotation(AuditAttributes.class).value();
    } else {
      auditAttributes = new AuditAttribute[] {element.getDeclaredAnnotation(AuditAttribute.class)};
    }
    return auditAttributes;
  }

  public static boolean isElementAnnotatedWithAuditAttribute(AnnotatedElement element) {
    return element.isAnnotationPresent(AuditAttribute.class)
        || element.isAnnotationPresent(AuditAttributes.class);
  }

  public Map<String, Object> processAnnotation(Object object)
      throws IllegalAccessException, NoSuchFieldException, NoSuchMethodException,
          InvocationTargetException, InstantiationException {
    if (nonNull(object)) {
      return parseObjectFieldsAndBuildAuditKeyValueMap(object);
    }
    return Collections.emptyMap();
  }
}
