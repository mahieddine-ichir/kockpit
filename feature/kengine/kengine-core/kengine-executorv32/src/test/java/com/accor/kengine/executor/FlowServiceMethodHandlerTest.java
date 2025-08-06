package com.accor.kengine.executor;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import com.accor.kengine.RuleNodeException;
import com.accor.kengine.execution.ExecutionResult;
import com.accor.kengine.executor.FlowServiceMethodHandler.ArgumentAuditFlagResolver;
import com.accor.kengine.executor.FlowServiceMethodHandler.ArgumentNamedExecutionResolver;
import com.accor.kengine.executor.FlowServiceMethodHandler.FlowIdNamedExecutionResolver;
import com.accor.kengine.executor.FlowServiceMethodHandler.FlowServiceCaller;
import com.accor.kengine.executor.FlowServiceMethodHandler.PropertyMetadata;
import com.accor.kengine.executor.FlowServiceMethodHandler.StaticAuditFlagResolver;
import com.accor.kengine.executor.model.User;
import com.accor.kengine.seamless.AuditFlowExecution;
import com.accor.kengine.seamless.ContextParameter;
import com.accor.kengine.seamless.ContextResult;
import com.accor.kengine.seamless.Flow;
import com.accor.kengine.seamless.NamedFlowExecution;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import lombok.Data;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotations;
import org.thepavel.icomponent.metadata.ClassMetadataImpl;
import org.thepavel.icomponent.metadata.MethodMetadataImpl;
import org.thepavel.icomponent.metadata.ResolvedTypeMetadataImpl;

@ExtendWith(MockitoExtension.class)
class FlowServiceMethodHandlerTest {

  private FlowServiceMethodHandler underTest;

  private KEngineFlowRunnerImpl kEngineFlowRunner;

  @Flow
  interface FakeSimpleFlow {
    String soSimple(String name, User user);
  }

  @Flow("theMultiAnnotationsFlow")
  interface MultiAnnotationsFlow {
    @ContextResult("inputName")
    @AuditFlowExecution(parameterName = "audit")
    @NamedFlowExecution(parameterName = "name")
    String moreComplex(
        boolean audit,
        @ContextParameter("inputName") String name,
        @ContextParameter("newUser") User user);
  }

  @Flow
  interface FlowComposition {
    ResultComposition returnAComposition(String name, User user);
  }

  @Data
  @ContextResult
  @Setter
  public static class ResultComposition {
    private String name;

    @ContextResult("user")
    private User currentUser;

    private ExecutionResult flowExecutionResult;
  }

  @BeforeEach
  void setup() {
    kEngineFlowRunner = Mockito.mock(KEngineFlowRunnerImpl.class);
    underTest = new FlowServiceMethodHandler(kEngineFlowRunner, Optional.empty());
  }

  @Test
  void should_handle_simple_case() {
    MethodMetadataImpl metadata = createFakeSimpleMethodMetadata();

    ExecutionResult executionResult = new ExecutionResult(Collections.emptyList());
    executionResult.setSuccessful(true);
    when(kEngineFlowRunner.execute(anyString(), any(), any(),anyBoolean())).thenReturn(executionResult);

    assertThatCode(() -> underTest.handle(new Object[] {null, null}, metadata))
        .doesNotThrowAnyException();
  }

  @Test
  void should_compute_flow_execution_without_method_annotation() {
    // Given
    MethodMetadataImpl metadata = createFakeSimpleMethodMetadata();

    // When
    FlowServiceCaller flowServiceCaller = underTest.getFlowServiceCaller(metadata);

    // Then
    String flowId = "fakeSimpleFlow";
    assertThat(flowServiceCaller).isNotNull();
    assertThat(flowServiceCaller.getFlowId()).isEqualTo(flowId);
    assertThat(flowServiceCaller.getParameterNames()).isEqualTo(Arrays.asList("name", "user"));
    assertThat(flowServiceCaller.getContextResult()).isNull();
    assertThat(flowServiceCaller.getReturnPropertyName()).isEqualTo("soSimple");
    assertThat(flowServiceCaller.getAuditFlagResolver())
        .isEqualTo(StaticAuditFlagResolver.INSTANCE_ENABLE);
    assertThat(flowServiceCaller.getNamedExecutionResolver())
        .isEqualTo(FlowIdNamedExecutionResolver.INSTANCE);
  }

  @Test
  void should_compute_flow_execution_with_method_annotations() {
    // Given
    MethodMetadataImpl metadata = createMultiAnnotationsMethodMetadata();

    // When
    FlowServiceCaller flowServiceCaller = underTest.getFlowServiceCaller(metadata);

    // Then
    String flowId = "theMultiAnnotationsFlow";
    assertThat(flowServiceCaller).isNotNull();
    assertThat(flowServiceCaller.getFlowId()).isEqualTo(flowId);
    assertThat(flowServiceCaller.getParameterNames())
        .isEqualTo(Arrays.asList("audit", "inputName", "newUser"));
    assertThat(flowServiceCaller.getContextResult()).isNotNull();
    assertThat(flowServiceCaller.getContextResult().propertyName()).isEqualTo("inputName");
    assertThat(flowServiceCaller.getContextResult().value()).isEqualTo("inputName");
    assertThat(flowServiceCaller.getReturnPropertyName()).isEqualTo("inputName");
    assertThat(flowServiceCaller.getAuditFlagResolver())
        .isEqualTo(new ArgumentAuditFlagResolver(0));
    assertThat(flowServiceCaller.getNamedExecutionResolver())
        .isEqualTo(new ArgumentNamedExecutionResolver(1));
  }

  @Test
  void should_compute_flow_execution_result_with_composition() {
    // Given
    MethodMetadataImpl metadata = createCompositionMethodMetadata();
    ExecutionResult executionResult = new ExecutionResult(new ArrayList<>());
    executionResult.setSuccessful(true);
    when(kEngineFlowRunner.execute(any(), any(), any(), anyBoolean())).thenReturn(executionResult);

    // When
    FlowServiceCaller flowServiceCaller = underTest.getFlowServiceCaller(metadata);

    // Then
    String flowId = "flowComposition";
    assertThat(flowServiceCaller).isNotNull();
    assertThat(flowServiceCaller.getFlowId()).isEqualTo(flowId);
    assertThat(flowServiceCaller.getParameterNames()).isEqualTo(Arrays.asList("name", "user"));
    assertThat(flowServiceCaller.getContextResult()).isNull();
    assertThat(flowServiceCaller.getReturnPropertyName()).isEqualTo("returnAComposition");
    assertThat(flowServiceCaller.getCompositeReturnMetadata()).isNotNull();
    assertThat(flowServiceCaller.getCompositeReturnMetadata().getType())
        .isEqualTo(ResultComposition.class);
    assertThat(
            flowServiceCaller.getCompositeReturnMetadata().getPropertyMetadataList().stream()
                .map(PropertyMetadata::getName)
                .toList())
        .isEqualTo(Arrays.asList("name", "currentUser", "flowExecutionResult"));
    assertThat(
            flowServiceCaller.getCompositeReturnMetadata().getPropertyMetadataList().stream()
                .map(PropertyMetadata::getSourceName)
                .toList())
        .isEqualTo(Arrays.asList("name", "user", "flowExecutionResult"));

    // When
    User user = new User();
    user.setId(10);
    String name = "my name";
    ResultComposition returnComposition =
        (ResultComposition) underTest.handle(new Object[] {name, user}, metadata);

    // Then
    assertThat(returnComposition).isNotNull();
    assertThat(returnComposition.getName()).isEqualTo(name);
    assertThat(returnComposition.getCurrentUser()).isEqualTo(user);
    assertThat(returnComposition.getFlowExecutionResult()).isEqualTo(executionResult);
  }

  private static MethodMetadataImpl createFakeSimpleMethodMetadata() {
    AnnotationUtils.getAnnotation(FakeSimpleFlow.class, Flow.class);
    MergedAnnotations mergedAnnotations = MergedAnnotations.from(FakeSimpleFlow.class);
    Method method = FakeSimpleFlow.class.getMethods()[0];

    MethodMetadataImpl metadata =
        new MethodMetadataImpl(
            new ClassMetadataImpl(FakeSimpleFlow.class, mergedAnnotations), method);
    metadata.setReturnTypeMetadata(
        new ResolvedTypeMetadataImpl(String.class, MergedAnnotations.from(String.class)));
    metadata.setAnnotations(MergedAnnotations.from(method));
    return metadata;
  }

  private static MethodMetadataImpl createMultiAnnotationsMethodMetadata() {
    MergedAnnotations mergedAnnotations = MergedAnnotations.from(MultiAnnotationsFlow.class);
    Method method = MultiAnnotationsFlow.class.getMethods()[0];

    MethodMetadataImpl metadata =
        new MethodMetadataImpl(
            new ClassMetadataImpl(MultiAnnotationsFlow.class, mergedAnnotations), method);
    metadata.setReturnTypeMetadata(
        new ResolvedTypeMetadataImpl(String.class, MergedAnnotations.from(method)));
    metadata.setAnnotations(MergedAnnotations.from(method));
    return metadata;
  }

  private static MethodMetadataImpl createCompositionMethodMetadata() {
    MergedAnnotations mergedAnnotations = MergedAnnotations.from(FlowComposition.class);
    Method method = FlowComposition.class.getMethods()[0];

    MethodMetadataImpl metadata =
        new MethodMetadataImpl(
            new ClassMetadataImpl(FlowComposition.class, mergedAnnotations), method);
    metadata.setReturnTypeMetadata(
        new ResolvedTypeMetadataImpl(ResultComposition.class, MergedAnnotations.from(method)));
    metadata.setAnnotations(MergedAnnotations.from(method));
    return metadata;
  }
}
