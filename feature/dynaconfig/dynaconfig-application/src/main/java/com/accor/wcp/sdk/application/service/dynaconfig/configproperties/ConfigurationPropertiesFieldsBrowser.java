package com.accor.wcp.sdk.application.service.dynaconfig.configproperties;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.accor.wcp.sdk.application.service.dynaconfig.DynaConfigAttribute;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@link ConfigurationProperties} Dynamic config fields browser. It browses and collect all dynamic
 * fields on a Configuration Properties (special case in Spring properties injection).
 */
@UtilityClass
public class ConfigurationPropertiesFieldsBrowser {

  public List<FoundField> readDynaConfigAttributeOn(Object target) {
    ConfigurationProperties anno = target.getClass().getAnnotation(ConfigurationProperties.class);
    if (isNull(anno)) {
      return emptyList();
    }
    String prefix = anno.prefix();
    return readDynamicConfigOnConfigurationPropertiesClass(
        prefix, DynaConfigAttribute.class, target);
  }

  static <T extends Annotation> List<FoundField> readDynamicConfigOnConfigurationPropertiesClass(
      String parent, Class<T> annotationClass, Object target) {
    if (isNull(target)) {
      return emptyList();
    }
    List<FoundField> properties = new ArrayList<>();
    // traversing the type hierarchy upward to process fields declared in classes target's class
    // extends
    Class<?> clazz = target.getClass();
    do {
      properties.addAll(
          findFieldsOnAnnotatedDeclaredFields(parent, annotationClass, target, clazz));
    } while ((clazz = clazz.getSuperclass()) != null);

    return properties;
  }

  private static <T extends Annotation> List<FoundField> findFieldsOnAnnotatedDeclaredFields(
      String parent, Class<T> annotationClass, Object target, Class<?> clazz) {
    Field[] fields = clazz.getDeclaredFields();
    List<FoundField> properties = new ArrayList<>();
    for (Field field : fields) {
      // Skip field starting with this$ to avoir infinite loop
      if (field.getName().startsWith("this$")) {
        break;
      }
      if (nonNull(field.getAnnotation(annotationClass))) {
        FoundField found = readFieldProperty(field, target, parent);
        properties.add(found);
      } else if (isAPojo(field)) {
        String hyphens = convertToLowerHyphens(field.getName());
        properties.addAll(
            readDynamicConfigOnConfigurationPropertiesClass(
                parent + "." + hyphens, annotationClass, get(field, target)));
      }
    }
    return properties;
  }

  private static boolean isAPojo(Field field) {
    return !field.getType().isPrimitive()
        && !field.getType().isArray()
        && !field.getType().isAssignableFrom(Collection.class)
        && !field.getType().isAssignableFrom(Map.class);
  }

  static String convertToLowerHyphens(String s) {
    return s.replaceAll("([a-z0-9])([A-Z]+)", "$1-$2").toLowerCase();
  }

  static FoundField readFieldProperty(Field field, Object target, String parent) {
    try {
      Object value = get(field, target);
      String propertyName = parent + "." + convertToLowerHyphens(field.getName());
      return FoundField.builder()
          .propertyName(propertyName)
          .targetObject(target)
          .field(field)
          .value(value)
          .build();
    } catch (Exception e) {
      String message =
          String.format(
              "Failed to read field %s", target.getClass().getSimpleName() + "." + field.getName());
      throw new IllegalStateException(message, e);
    }
  }

  static Object get(Field field, Object target) {
    try {
      makeFieldAccessibleNotFinal(field, target);
      return field.get(target);
    } catch (Exception e) {
      String message =
          String.format(
              "Failed to get the value on field %s",
              target.getClass().getSimpleName() + "." + field.getName());
      throw new IllegalStateException(message, e);
    }
  }

  static void makeFieldAccessibleNotFinal(Field field, Object target) {
    try {
      field.setAccessible(true);
    } catch (Exception e) {
      String message = String.format("Failed to make field %s accessible", field.getName());
      throw new IllegalStateException(message, e);
    }
  }
}
