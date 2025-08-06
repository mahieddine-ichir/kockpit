package com.accor.wcp.audit.module.kengine.flow;

import static java.util.Arrays.asList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.accor.kengine.execution.ExecutionResult;
import com.accor.kengine.executor.FlowHandlerExecutionContext;
import com.accor.wcp.audit.IndexedKeyValue;
import com.accor.wcp.audit.annotation.AuditAttribute;
import com.accor.wcp.audit.annotation.AuditAttributesAnnotationProcessor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.MergedAnnotations;
import org.thepavel.icomponent.metadata.ClassMetadataImpl;
import org.thepavel.icomponent.metadata.MethodMetadataImpl;

class FlowExecutionKeyValuesTest {

  @Data
  @Builder
  public static class SubObject {
    String id;
  }

  @Data
  @Builder
  public static class Result {
    private SubObject subObject;
    private List<SubObject> subObjects;
    private String name;

    public String subObjectsIds() {
      return subObjects.stream().map(SubObject::getId).collect(Collectors.joining());
    }

    public String getSubObjectsIds() {
      return subObjects.stream().map(SubObject::getId).collect(Collectors.joining());
    }
  }

  @AuditAttribute(key = "subId", path = "subObject.id")
  @AuditAttribute(key = "resultName", path = "name")
  @AuditAttribute(key = "ctxAttribute", path = "$context.attribute1")
  @AuditAttribute(key = "ctxInnerAttribute", path = "$context.anotherObject.subObject.id")
  @AuditAttribute(key = "ctxSubObjectIds", path = "$context.anotherObject.subObjectsIds")
  @AuditAttribute(
      key = "ctxInnerAttributeFailed",
      path = "$context.anotherObject.subObject.notFound")
  public Result exampleInputMethod(
      @AuditAttribute String parameter1, @AuditAttribute String parameter2) {
    // Not needed
    return null;
  }

  @AuditAttribute
  public String exampleReturnOnly() {
    // Not needed
    return "exampleReturnOnly-1";
  }

  @Test
  void should_getIndexedKeyValues_complexe_input_method() throws NoSuchMethodException {
    // Given
    Method method = getClass().getMethod("exampleInputMethod", String.class, String.class);
    MethodMetadataImpl methodMetadata =
        new MethodMetadataImpl(
            new ClassMetadataImpl(getClass(), MergedAnnotations.of(new ArrayList<>())), method);
    ExecutionResult executionResult = new ExecutionResult(new ArrayList<>());

    Result result =
        Result.builder()
            .subObject(SubObject.builder().id("SUBOBJ1").build())
            .subObjects(
                asList(
                    SubObject.builder().id("SUBOBJ1.1").build(),
                    SubObject.builder().id("SUBOBJ1.2").build(),
                    SubObject.builder().id("SUBOBJ1.3").build()))
            .name("Cyril")
            .build();

    Map<Object, Object> mapContext = new HashMap<>();
    mapContext.put("attribute1", "valueFromContextToAudit");
    mapContext.put("anotherObject", result);

    FlowHandlerExecutionContext flowHandlerExecutionContext =
        FlowHandlerExecutionContext.builder()
            .methodMetadata(methodMetadata)
            .arguments(new Object[] {"value1", "value2"})
            .result(result)
            .build();
    mapContext.put(FlowHandlerExecutionContext.class, flowHandlerExecutionContext);

    // When
    List<IndexedKeyValue> indexedKeyValues =
        FlowExecutionKeyValues.getIndexedKeyValues(
            mapContext, executionResult, new AuditAttributesAnnotationProcessor());

    // Then
    assertThat(indexedKeyValues).isNotNull().hasSize(9);
  }

  @Test
  void should_getIndexedKeyValues_simple_input_method() throws NoSuchMethodException {
    // Given
    Map<Object, Object> mapContext = new HashMap<>();
    Method method = getClass().getMethod("exampleReturnOnly");
    MethodMetadataImpl methodMetadata =
        new MethodMetadataImpl(
            new ClassMetadataImpl(getClass(), MergedAnnotations.of(new ArrayList<>())), method);
    ExecutionResult executionResult = new ExecutionResult(new ArrayList<>());

    FlowHandlerExecutionContext flowHandlerExecutionContext =
        FlowHandlerExecutionContext.builder()
            .methodMetadata(methodMetadata)
            .arguments(null)
            .result("StringResult")
            .build();
    mapContext.put(FlowHandlerExecutionContext.class, flowHandlerExecutionContext);

    // When
    List<IndexedKeyValue> indexedKeyValues =
        FlowExecutionKeyValues.getIndexedKeyValues(
            mapContext, executionResult, new AuditAttributesAnnotationProcessor());

    // Then
    assertThat(indexedKeyValues)
        .isNotNull()
        .hasSize(3)
        .containsAnyOf(IndexedKeyValue.of("exampleReturnOnly", "StringResult"));

    // When
    flowHandlerExecutionContext.setResult(10);
    indexedKeyValues =
        FlowExecutionKeyValues.getIndexedKeyValues(
            mapContext, executionResult, new AuditAttributesAnnotationProcessor());

    // Then
    assertThat(indexedKeyValues)
        .isNotNull()
        .hasSize(3)
        .containsAnyOf(new IndexedKeyValue("exampleReturnOnly", 10));
  }
}
