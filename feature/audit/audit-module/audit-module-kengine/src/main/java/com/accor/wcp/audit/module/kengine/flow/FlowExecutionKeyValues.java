package com.accor.wcp.audit.module.kengine.flow;

import static com.accor.wcp.audit.annotation.AuditAttributesAnnotationProcessor.extractKeyValueListFromObjectField;
import static com.accor.wcp.audit.annotation.AuditAttributesAnnotationProcessor.getAuditAttributes;
import static com.accor.wcp.audit.annotation.AuditAttributesAnnotationProcessor.isElementAnnotatedWithAuditAttribute;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.accor.kengine.audit.model.ResultStatus;
import com.accor.kengine.execution.ExecutionResult;
import com.accor.kengine.executor.FlowHandlerExecutionContext;
import com.accor.wcp.audit.IndexedKeyValue;
import com.accor.wcp.audit.annotation.AuditAttribute;
import com.accor.wcp.audit.annotation.AuditAttributesAnnotationProcessor;
import com.accor.wcp.flow.FlowContextContainer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;

// TODO CJO - Unit tests
@Slf4j
public class FlowExecutionKeyValues {

  public static final String ATTRIBUT_PATH_PREFIX_ROOT = "$context.";

  public static List<IndexedKeyValue> getIndexedKeyValues(
      Object context,
      ExecutionResult executionResult,
      AuditAttributesAnnotationProcessor annotationProcessor) {
    List<IndexedKeyValue> indexedKeyValues =
        new ArrayList<>(auditContext(context, annotationProcessor));
    indexedKeyValues.add(IndexedKeyValue.of("KId", executionResult.getExecutionId()));
    indexedKeyValues.add(IndexedKeyValue.of("KResult", getResult(executionResult)));

    return indexedKeyValues;
  }

  private static ResultStatus getResult(ExecutionResult executionResult) {
    if (!executionResult.isSuccessful()) {
      if (executionResult.isWarning()) {
        return ResultStatus.WARNING;
      } else {
        return ResultStatus.ERROR;
      }
    } else if (executionResult.isWarning()) {
      return ResultStatus.WARNING;
    } else {
      return ResultStatus.VALID;
    }
  }

  private static List<IndexedKeyValue> auditContext(
      Object context, AuditAttributesAnnotationProcessor annotationProcessor) {
    if (isNull(context)) {
      return emptyList();
    }

    if (isNull(annotationProcessor)) {
      return emptyList();
    }

    try {
      Map<String, Object> auditedFields = new HashMap<>();
      if (context instanceof FlowContextContainer flowContextContainer) {
        for (Entry<Class<?>, Object> entry : flowContextContainer.getContextData().entrySet()) {
          auditedFields.putAll(annotationProcessor.processAnnotation(entry.getValue()));
        }
      } else if (context instanceof Map<?, ?> mapContext) {
        for (Object contextValue : mapContext.values()) {
          auditedFields.putAll(annotationProcessor.processAnnotation(contextValue));
        }
        // Special case for flow handler inputs for audit
        FlowHandlerExecutionContext flowHandlerExecutionContext =
            (FlowHandlerExecutionContext) mapContext.get(FlowHandlerExecutionContext.class);
        if (nonNull(flowHandlerExecutionContext)) {
          auditedFields.putAll(
              processFlowHandlerExecutionContext(flowHandlerExecutionContext, mapContext));
        }
      } else {
        auditedFields.putAll(annotationProcessor.processAnnotation(context));
      }
      return auditedFields.entrySet().stream()
          .map(m -> IndexedKeyValue.of(m.getKey(), m.getValue()))
          .toList();
    } catch (Exception e) {
      log.error("Unable to audit context : {}", e.getMessage(), e);
      return emptyList();
    }
  }

  private static Map<String, Object> processFlowHandlerExecutionContext(
      FlowHandlerExecutionContext flowHandlerExecutionContext, Map<?, ?> mapContext)
      throws NoSuchFieldException, InvocationTargetException, IllegalAccessException,
          NoSuchMethodException, InstantiationException {
    Map<String, Object> auditedFields = new HashMap<>();
    Method sourceMethod = flowHandlerExecutionContext.getMethodMetadata().getSourceMethod();

    // Inputs
    Parameter[] parameters = sourceMethod.getParameters();
    for (int i = 0; i < parameters.length; i++) {
      Parameter parameter = parameters[i];
      if (isElementAnnotatedWithAuditAttribute(parameter)) {
        AuditAttribute[] auditAttributes = getAuditAttributes(parameter);
        Optional<Object> value = Optional.ofNullable(flowHandlerExecutionContext.getArguments()[i]);
        extractKeyValueListFromObjectField(auditAttributes, parameter.getName(), value)
            .forEach(entry -> auditedFields.put(entry.getKey(), entry.getValue()));
      }
    }

    // Return (method)
    if (isElementAnnotatedWithAuditAttribute(sourceMethod)) {
      // Dispatch between "root" and "result"
      List<AuditAttribute> allAuditAttributes = Arrays.asList(getAuditAttributes(sourceMethod));
      List<AuditAttribute> auditAttributesRoot =
          allAuditAttributes.stream()
              .filter(auditAttribute -> auditAttribute.path().startsWith(ATTRIBUT_PATH_PREFIX_ROOT))
              .toList();
      List<AuditAttribute> auditAttributesResult = new ArrayList<>(allAuditAttributes);
      auditAttributesResult.removeAll(auditAttributesRoot);

      extractKeyValueListFromObjectField(
              auditAttributesResult.toArray(new AuditAttribute[] {}),
              sourceMethod.getName(),
              Optional.ofNullable(flowHandlerExecutionContext.getResult()))
          .forEach(entry -> auditedFields.put(entry.getKey(), entry.getValue()));

      // Special case to audit specific audit attribute
      auditAttributesRoot.forEach(
          auditAttribute -> {
            try {
              String path = auditAttribute.path().replace(ATTRIBUT_PATH_PREFIX_ROOT, "");
              Object nestedProperty = PropertyUtils.getNestedProperty(mapContext, path);
              auditedFields.put(auditAttribute.key(), nestedProperty);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
              // Nothing to do
              log.info(
                  "Can not get audit attribute value from context (key: {} path: {}). Error: {}",
                  auditAttribute.key(),
                  auditAttribute.path(),
                  e.getMessage());
            }
          });
    }

    return auditedFields;
  }
}
