package com.accor.wcp.flow;

import com.accor.kengine.DefaultDocumentationDetails;
import com.accor.kengine.RuleNodeException;
import com.accor.kengine.RuleNodeExecutor;
import com.accor.kengine.execution.ExecutionResult;
import com.accor.kengine.registry.RuleNodeRegistry;
import com.accor.wcp.flow.errors.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class FlowRunnerImplTest {

  private FlowRunnerImpl underTest;
  private RuleNodeExecutorFactory factory;
  private DefaultKEngineFlowRunnerExecutionHandler flowRunnerExecutionHandler;

  @BeforeEach
  void setup() throws RuleNodeException {
    RuleNodeRegistry registry = Mockito.mock(RuleNodeRegistry.class);
    factory = Mockito.mock(RuleNodeExecutorFactory.class);
    flowRunnerExecutionHandler = new DefaultKEngineFlowRunnerExecutionHandler();
    underTest = new FlowRunnerImpl(registry, factory, flowRunnerExecutionHandler);
  }

  @Test
  void should_execute_normally() {
    // Given
    RuleNodeExecutor<Object> ruleNodeExecutor = Mockito.mock(RuleNodeExecutor.class);
    when(factory.createRuleNodeExecutor(any(), anyBoolean())).thenReturn(ruleNodeExecutor);
    ExecutionResult executionResult = new ExecutionResult(emptyList());
    executionResult.setSuccessful(true);
    when(ruleNodeExecutor.execute(anyList(), any())).thenReturn(executionResult);
    DefaultDocumentationDetails flowDetails = new DefaultDocumentationDetails("flow", "");
    String executionName = "executionName";
    FlowContextContainer context = new FlowContextContainer();

    // When
    ExecutionResult executionResultReturned =
        underTest.execute(flowDetails, context, executionName, true);

    // Then
    assertThat(executionResultReturned).isNotNull();
    assertThat(executionResultReturned).isEqualTo(executionResult);
  }

  @Test
  void should_execute_normally_wrapper_methods() {
    // Test 'wrapper' methods

    // Given
    RuleNodeExecutor<Object> ruleNodeExecutor = Mockito.mock(RuleNodeExecutor.class);
    when(factory.createRuleNodeExecutor(any(), anyBoolean())).thenReturn(ruleNodeExecutor);
    when(factory.createRuleNodeExecutor(any())).thenReturn(ruleNodeExecutor);
    when(factory.createRuleNodeExecutor(anyBoolean())).thenReturn(ruleNodeExecutor);
    ExecutionResult executionResult = new ExecutionResult(emptyList());
    executionResult.setSuccessful(true);
    when(ruleNodeExecutor.execute(anyList(), any())).thenReturn(executionResult);
    DefaultDocumentationDetails flowDetails = new DefaultDocumentationDetails("flow", "");
    String executionName = "executionName";
    FlowContextContainer context = new FlowContextContainer();

    // When
    ExecutionResult executionResultReturned =
        underTest.executeAndGetResult(flowDetails, context, executionName, true);
    assertThat(executionResultReturned).isEqualTo(executionResult);
    // When
    executionResultReturned = underTest.execute(flowDetails, context, executionName);
    assertThat(executionResultReturned).isEqualTo(executionResult);
    // When
    executionResultReturned = underTest.execute(flowDetails, context, false);
    assertThat(executionResultReturned).isEqualTo(executionResult);
    // When
    executionResultReturned = underTest.execute(flowDetails, context);
    assertThat(executionResultReturned).isEqualTo(executionResult);

    // When
    executionResultReturned = underTest.executeAndGetResult(flowDetails, context);
    assertThat(executionResultReturned).isEqualTo(executionResult);

    // When
    executionResultReturned = underTest.executeAndGetResult(flowDetails, context, false);
    assertThat(executionResultReturned).isEqualTo(executionResult);
  }

  @Test
  void should_execute_error_case() {
    // Given
    RuleNodeExecutor<Object> ruleNodeExecutor = Mockito.mock(RuleNodeExecutor.class);
    when(factory.createRuleNodeExecutor(any(), anyBoolean())).thenReturn(ruleNodeExecutor);
    ExecutionResult executionResult = new ExecutionResult(emptyList());
    executionResult.setSuccessful(false);
    executionResult.setThrowable(
        new RuleNodeException(null, new IllegalStateException("Root Exception Message")));
    when(ruleNodeExecutor.execute(anyList(), any())).thenReturn(executionResult);

    // When
    FlowContextContainer context = new FlowContextContainer();
    assertThatCode(
            () ->
                underTest.execute(
                    new DefaultDocumentationDetails("flow", ""), context, "executionName", true))
        .hasMessage("Internal Server error");

    // When
    ErrorCodeImpl errorCode =
        new ErrorCodeImpl("WarningStop", 400, "Details", new HashMap<>(), "name");
    FlowExecutionInterruptWarning flowExecutionInterruptWarning =
        new FlowExecutionInterruptWarning(errorCode);
    executionResult.setThrowable(new RuleNodeException(null, flowExecutionInterruptWarning));
    assertThatCode(
            () ->
                underTest.execute(
                    new DefaultDocumentationDetails("flow", ""), context, "executionName", true))
        .hasMessage("WarningStop")
        .isEqualTo(flowExecutionInterruptWarning);

    // When
    errorCode = new ErrorCodeImpl("Error", 400, "Details", new HashMap<>(), "name");
    FlowExecutionError flowExecutionError = new FlowExecutionError(errorCode);
    executionResult.setThrowable(new RuleNodeException(null, flowExecutionError));
    assertThatCode(
            () ->
                underTest.execute(
                    new DefaultDocumentationDetails("flow", ""), context, "executionName", true))
        .hasMessage("Error")
        .isEqualTo(flowExecutionError);
  }

  @Test
  void should_execute_warnings_case() {
    // Given
    RuleNodeExecutor<Object> ruleNodeExecutor = Mockito.mock(RuleNodeExecutor.class);
    when(factory.createRuleNodeExecutor(any(), anyBoolean())).thenReturn(ruleNodeExecutor);
    ExecutionResult executionResult = new ExecutionResult(emptyList());
    executionResult.setSuccessful(true);
    executionResult.setWarning(true);
    executionResult.setThrowable(
        new RuleNodeException(null, new IllegalStateException("Root Exception Message")));
    when(ruleNodeExecutor.execute(anyList(), any())).thenReturn(executionResult);

    // When
    FlowContextContainer context = new FlowContextContainer();
    assertThatCode(
            () ->
                underTest.execute(
                    new DefaultDocumentationDetails("flow", ""), context, "executionName", true))
        .doesNotThrowAnyException();

    // Given Flow warning exception
    ErrorCodeImpl errorCode = new ErrorCodeImpl("Bad", 400, "Details", new HashMap<>(), "name");
    executionResult.setThrowable(null);
    executionResult.addWarning(new FlowExecutionWarning(errorCode));
    executionResult.addWarning(new FlowExecutionMultiWarning(Arrays.asList(errorCode)));
    assertThatCode(
            () ->
                underTest.execute(
                    new DefaultDocumentationDetails("flow", ""), context, "executionName", true))
        .doesNotThrowAnyException();
  }
}
