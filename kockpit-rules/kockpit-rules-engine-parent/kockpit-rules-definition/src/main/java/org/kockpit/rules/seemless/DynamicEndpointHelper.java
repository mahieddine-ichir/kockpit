package org.kockpit.rules.seemless;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

@UtilityClass
@Slf4j
final class DynamicEndpointHelper {

  @Data
  @Builder
  static class DynamicParameter {
    private String name;
    private Class<?> type;
    private Parameter parameter;
    private boolean dynamic;
    private boolean errorOnNulls;
  }

  @Data
  @AllArgsConstructor
  public static class DynamicEndpoint<T> {
    private T annotation;
    private Method method;
    private List<DynamicParameter> parameters;

    Object getInvokeResult(Object toHandle, Object context)
        throws IllegalAccessException, InvocationTargetException {

      Object[] args = new Object[0];
      try {
        if (parameters.isEmpty()) {
          // No param
          return method.invoke(toHandle);
        } else {
          // resolve and inject parameters
          args = resolveArguments(context);
          return method.invoke(toHandle, args);
        }
      } catch (IllegalArgumentException e) {
        logMismatchParameterTypes(args);
        throw e;
      }
    }

    private void logMismatchParameterTypes(Object[] args) {
      log.error("Argument type mismatch on dynamic call: {}", this);
      for (int i = 0; i < parameters.size(); i++) {
        // Type difference?
        Class<?> expected = parameters.get(i).getType();
        if (args[i] != null) {
          Object actual = args[i].getClass();
          if (!expected.equals(actual)) {
            log.error("  - Argument {} expected: {}, actual: {}", i, expected, actual);
          }
        }
      }
    }

    Object[] resolveArguments(Object context) {
      boolean forceDynamic = context instanceof Map;
      return resolveArguments(forceDynamic, context);
    }

    Object[] resolveArguments(boolean forceDynamic, Object context) {
      List<Object> resolvedArguments =
          parameters.stream()
              .map(parameter -> resolveArgument(forceDynamic, context, parameter))
              .toList();
      return resolvedArguments.toArray(new Object[0]);
    }

    private Object resolveArgument(
        boolean forceDynamic, Object context, DynamicParameter parameter) {
      // Case of input context
      if (parameter.getType() == context.getClass()) {
        return context;
      }

      // Else go into context to find data to inject
      try {
        if (forceDynamic || parameter.isDynamic()) {
          return PropertyUtils.getNestedProperty(context, parameter.getName());
        } else {
          return PropertyUtils.getSimpleProperty(context, parameter.getName());
        }
      } catch (IllegalAccessException e) {
        log.info(
            "Error getting value (illegal access) for context parameter: {}. Error: {}",
            parameter,
            e.getMessage());
      } catch (InvocationTargetException e) {
        log.info(
            "Error getting value (invocation target) for context parameter: {}. Error: {}",
            parameter,
            e.getMessage());
      } catch (NoSuchMethodException e) {
        log.info(
            "Error getting value (not found) for context parameter: {}. Error: {}",
            parameter,
            e.getMessage());
      } catch (NestedNullException | NullPointerException e) {
        // Skip or not nulls?
        if (parameter.errorOnNulls) {
          throw e;
        }
      }
      return null;
    }
  }

  static <T extends Annotation> DynamicEndpoint<T> readDynamicEndpoint(
      Object object, Class<T> annotationClass) {
    return readDynamicEndpoint(object, null, annotationClass);
  }

  static <T extends Annotation> DynamicEndpoint<T> readDynamicEndpoint(
      Object object, Method methodSignature, Class<T> annotationClass) {

    // First annotated method
    Method[] methods = AopUtils.getTargetClass(object).getDeclaredMethods();
    List<Method> handlerAnnotatedMethods =
        Arrays.stream(methods).filter(m -> m.isAnnotationPresent(annotationClass)).toList();

    if (handlerAnnotatedMethods.isEmpty() && methods.length == 1) {
      handlerAnnotatedMethods = Arrays.stream(methods).toList();
    } else if (handlerAnnotatedMethods.size() > 1 && nonNull(methodSignature)) {
      // Filter from method signature
      handlerAnnotatedMethods =
          handlerAnnotatedMethods.stream()
              .filter(method -> hasSameSignature(methodSignature, method))
              .toList();
    }

    if (handlerAnnotatedMethods.isEmpty()) {
      throw new IllegalStateException("No method annotated with " + annotationClass);
    } else if (handlerAnnotatedMethods.size() > 1) {
      throw new IllegalStateException("Only one method must be annotated with " + annotationClass);
    }

    Method method = handlerAnnotatedMethods.get(0);
    method.setAccessible(true);
    Parameter[] parameters = method.getParameters();

    T annotation = method.getAnnotation(annotationClass);

    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    List<DynamicParameter> dynamicParameters = new ArrayList<>(parameters.length);
    for (int i = 0; i < parameters.length; i++) {
      Annotation[] annotations = parameterAnnotations[i];
      DynamicParameter dynamicParameter = toDynamicParameter(parameters[i], annotations);
      dynamicParameters.add(dynamicParameter);
    }

    return new DynamicEndpoint(annotation, method, dynamicParameters);
  }

  private static boolean hasSameSignature(Method methodSignature, Method method) {
    // FIXME - Should we use directly method#equals?
    if (!method.getName().equals(methodSignature.getName())) return false;
    if (method.getReturnType() != methodSignature.getReturnType()) return false;
    return Arrays.equals(method.getParameterTypes(), methodSignature.getParameterTypes());
  }

  private static DynamicParameter toDynamicParameter(
      Parameter parameter, Annotation[] annotations) {
    String name = parameter.getName();
    boolean dynamic = false;
    boolean errorOnNulls = false;

    // Is there any context parameter annotation?

    for (Annotation annotation : annotations) {
      if (annotation.annotationType() == ContextParameter.class) {
        ContextParameter contextParameter = (ContextParameter) annotation;
        String value = contextParameter.value();
        errorOnNulls = contextParameter.errorOnNulls();
        if (StringUtils.hasText(value)) {
          name = value;
          // Bean utils dynamic case
          if (name.contains(".") || name.contains("(") || name.contains("[")) {
            dynamic = true;
          }
        }
      }
    }

    return DynamicParameter.builder()
        .type(parameter.getType())
        .name(name)
        .parameter(parameter)
        .dynamic(dynamic)
        .errorOnNulls(errorOnNulls)
        .build();
  }
}
