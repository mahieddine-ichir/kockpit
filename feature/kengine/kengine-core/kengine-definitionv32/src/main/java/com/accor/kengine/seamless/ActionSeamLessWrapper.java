package com.accor.kengine.seamless;

import static com.accor.kengine.seamless.DynamicEndpointHelper.readDynamicEndpoint;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.ObjectUtils.isEmpty;

import com.accor.kengine.Action;
import com.accor.kengine.DefaultDocumentationDetails;
import com.accor.kengine.DocumentationDetails;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.core.annotation.MergedAnnotations;

@Slf4j
public class ActionSeamLessWrapper implements Action<Object> {
  private Object action;
  private Method methodSignature;
  private Action<Object> oldCompatibilityAction;
  private DocumentationDetails documentation;
  private DynamicEndpointHelper.DynamicEndpoint<com.accor.kengine.seamless.Action> dynamicEndpoint;
  private ContextResult contextResult;
  private String propertyNameInContextToReturn;
  private boolean returnValueInContextFlag;

  public ActionSeamLessWrapper(Object action, DocumentationDetails details) {
    this(action, null, details);
  }

  public ActionSeamLessWrapper(Object action, Object methodSignature, DocumentationDetails details) {
    this.action = action;
    // Only to delegate method signature recording
    if (methodSignature instanceof Runnable) {
      ((Runnable) methodSignature).run();
    }
    // Read last executed method and reset
    // Special case for getting / computing method pointer
    this.methodSignature = MultipleActionMethodReferenceRecorderInterceptor.lastExecutedMethod;
    MultipleActionMethodReferenceRecorderInterceptor.lastExecutedMethod = null;

    computeMethodReference();

    if (isNull(details)) {
      computeDocumentation();
    } else {
      documentation = details;
    }
  }

  private void computeDocumentation() {
    // Old compatibility
    if (action instanceof Action<?>) {
      documentation = ((Action<?>) action).getDetails();
      return;
    }

    // Dynamic annotated method
    if (isNull(dynamicEndpoint)) {
      return;
    }
    com.accor.kengine.seamless.Action actionAnnotation = dynamicEndpoint.getAnnotation();
    String value = actionAnnotation.value();
    if (isEmpty(value)) {
      value = action.getClass().getCanonicalName();
    }
    documentation = new DefaultDocumentationDetails(value, actionAnnotation.documentation());
  }

  void computeMethodReference() {
    if (action instanceof String) {
      // Nothing to do ... maybe next step is SPEL
      log.error("Action class String is not yet implemented (SPEL is coming ...)");
      return;
    }
    // Old compatibility
    if (action instanceof Action<?>) {
      this.oldCompatibilityAction = (Action) action;
      this.documentation = oldCompatibilityAction.getDetails();
      return;
    }

    // Dynamic way of life
    computeDynamicEndpointMetadata();

    // Must we put back result into context?
    computeReturnValueMetadata();
  }

  private void computeDynamicEndpointMetadata() {
    dynamicEndpoint =
        readDynamicEndpoint(action, methodSignature, com.accor.kengine.seamless.Action.class);
  }

  private void computeReturnValueMetadata() {
    ContextResult[] contextResults =
        dynamicEndpoint.getMethod().getAnnotationsByType(ContextResult.class);
    returnValueInContextFlag = false;
    if (isEmpty(contextResults)) {
      return;
    }
    returnValueInContextFlag = true;
    contextResult = MergedAnnotations.from(contextResults[0]).get(ContextResult.class).synthesize();
    propertyNameInContextToReturn = contextResult.propertyName();
    if (isBlank(propertyNameInContextToReturn)) {
      propertyNameInContextToReturn = dynamicEndpoint.getMethod().getName();
    }
  }

  @Override
  public void execute(Object context) throws Exception {
    // Old compatibility
    if (nonNull(this.oldCompatibilityAction)) {
      this.oldCompatibilityAction.execute(context);
      return;
    }

    // No parameter injection
    Object invokeResult = null;
    try {
      invokeResult = dynamicEndpoint.getInvokeResult(action, context);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof Exception ex) {
        throw ex;
      } else {
        throw e;
      }
    }

    // Nothing to return
    if (!returnValueInContextFlag) {
      return;
    }

    // Put result into context (even if it is a null one)
    updateContextWithReturnedValue(context, invokeResult);
  }

  private void updateContextWithReturnedValue(Object context, Object invokeResult) {
    try {
      PropertyUtils.setNestedProperty(context, propertyNameInContextToReturn, invokeResult);
    } catch (IllegalAccessException | InvocationTargetException e) {
      log.warn(
          "Field for result: {} (error) in context: {}", propertyNameInContextToReturn, context);
    } catch (NoSuchMethodException e) {
      log.warn(
          "Field for result: {} not found in context: {}", propertyNameInContextToReturn, context);
    }
  }

  @Override
  public DocumentationDetails getDetails() {
    if (isNull(documentation)) {
      documentation = new DefaultDocumentationDetails("undefined", "null");
    }
    return documentation;
  }
}
