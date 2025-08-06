package com.accor.kengine.seamless;

import static com.accor.kengine.seamless.DynamicEndpointHelper.readDynamicEndpoint;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.util.ObjectUtils.isEmpty;

import com.accor.kengine.DefaultDocumentationDetails;
import com.accor.kengine.DocumentationDetails;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Predicate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class PredicateSeamLessWrapper implements Predicate<Object> {
  private Object predicate;

  private Predicate<Object> oldCompatibilityPredicate;

  private DocumentationDetails documentation;
  private DynamicEndpointHelper.DynamicEndpoint<com.accor.kengine.seamless.Predicate>
      dynamicEndpoint;

  PredicateSeamLessWrapper(Object predicate, DocumentationDetails details) {
    this.predicate = predicate;

    computeMethodReference();

    if (isNull(details)) {
      computeDocumentation();
    } else {
      this.documentation = details;
    }
  }

  private void computeDocumentation() {
    if (isNull(dynamicEndpoint)) {
      return;
    }
    com.accor.kengine.seamless.Predicate predicateAnnotation = dynamicEndpoint.getAnnotation();
    String value = predicateAnnotation.value();
    if (isEmpty(value)) {
      value = predicate.getClass().getCanonicalName();
    }
    documentation = new DefaultDocumentationDetails(value, predicateAnnotation.documentation());
  }

  void computeMethodReference() {
    if (predicate instanceof String) {
      // Nothing to do ... maybe next step is SPEL
      log.error("Predicate class String is not yet implemented (SPEL is coming ...)");
      return;
    }
    // Old compatibility
    if (predicate instanceof Predicate predicate) {
      this.oldCompatibilityPredicate = predicate;
      return;
    }

    // Dynamic way of life
    computeDynamicEndpointMetadata();

    // Check return type
    checkReturnType();
  }

  private void checkReturnType() {
    Method method = dynamicEndpoint.getMethod();
    if (method.getReturnType() != boolean.class) {
      throw new IllegalStateException("Method must return a boolean value");
    }
  }

  private void computeDynamicEndpointMetadata() {
    dynamicEndpoint = readDynamicEndpoint(predicate, com.accor.kengine.seamless.Predicate.class);
  }

  public DocumentationDetails getDocumentation() {
    if (isNull(documentation)) {
      documentation = new DefaultDocumentationDetails("undefined", "null");
    }
    return documentation;
  }

  @SneakyThrows
  @Override
  public boolean test(Object context) {
    // Old compatibility
    if (nonNull(this.oldCompatibilityPredicate)) {
      return this.oldCompatibilityPredicate.test(context);
    }

    // Dynamic way of life
    Object invokeResult;
    try {
      invokeResult = dynamicEndpoint.getInvokeResult(predicate, context);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof Exception ex) {
        throw ex;
      } else {
        throw e;
      }
    }
    return (boolean) invokeResult;
  }
}
