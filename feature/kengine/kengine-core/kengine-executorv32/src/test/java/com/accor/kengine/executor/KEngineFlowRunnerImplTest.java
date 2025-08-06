package com.accor.kengine.executor;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.accor.kengine.KEngineRuleNodeExecutorFactory;
import com.accor.kengine.RuleNodeException;
import com.accor.kengine.RuleNodeExecutor;
import com.accor.kengine.execution.ExecutionResult;
import com.accor.kengine.registry.RuleNodeRegistry;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KEngineFlowRunnerImplTest {

  private KEngineFlowRunnerImpl underTest;
  private KEngineRuleNodeExecutorFactory factory;

  @BeforeEach
  void setup() throws RuleNodeException {
    RuleNodeRegistry registry = Mockito.mock(RuleNodeRegistry.class);
    factory = Mockito.mock(KEngineRuleNodeExecutorFactory.class);
    KEngineFlowRunnerExecutionHandler kEngineFlowRunnerExecutionHandler = null;
    underTest =
        new KEngineFlowRunnerImpl(
            registry, factory, Optional.ofNullable(kEngineFlowRunnerExecutionHandler));
  }

  @Test
  void should_execute_normal_case() {
    // Given
    RuleNodeExecutor<Object> ruleNodeExecutor = Mockito.mock(RuleNodeExecutor.class);
    when(factory.createRuleNodeExecutor(any(), anyBoolean())).thenReturn(ruleNodeExecutor);
    ExecutionResult executionResult = new ExecutionResult(Collections.emptyList());
    executionResult.setSuccessful(true);
    when(ruleNodeExecutor.execute(anyList(), any())).thenReturn(executionResult);

    // When
    ExecutionResult executionResultReturned =
        underTest.execute("flow", "Context", "executionName", true);

    // Then
    assertThat(executionResultReturned).isNotNull();
    assertThat(executionResultReturned).isEqualTo(executionResult);
  }

  // No more throw exception here
//  @Test
//  void should_execute_error_case() {
//    // Given
//    RuleNodeExecutor<Object> ruleNodeExecutor = Mockito.mock(RuleNodeExecutor.class);
//    when(factory.createRuleNodeExecutor(any(), anyBoolean())).thenReturn(ruleNodeExecutor);
//    ExecutionResult executionResult = new ExecutionResult(Collections.emptyList());
//    executionResult.setSuccessful(false);
//    executionResult.setThrowable(
//        new RuleNodeException(null, new IllegalStateException("Root Exception Message")));
//    when(ruleNodeExecutor.execute(anyList(), any())).thenReturn(executionResult);
//
//    // When
//    assertThatCode(() -> underTest.execute("flow", "Context", "executionName", true))
//        .hasMessage(
//            "com.accor.kengine.RuleNodeException: java.lang.IllegalStateException: Root Exception Message");
//  }

  @Test
  void should_execute_warnings_case() {
    // Given
    RuleNodeExecutor<Object> ruleNodeExecutor = Mockito.mock(RuleNodeExecutor.class);
    when(factory.createRuleNodeExecutor(any(), anyBoolean())).thenReturn(ruleNodeExecutor);
    ExecutionResult executionResult = new ExecutionResult(Collections.emptyList());
    executionResult.setSuccessful(true);
    executionResult.setWarning(true);
    executionResult.setThrowable(
        new RuleNodeException(null, new IllegalStateException("Root Exception Message")));
    when(ruleNodeExecutor.execute(anyList(), any())).thenReturn(executionResult);

    // When
    assertThatCode(() -> underTest.execute("flow", "Context", "executionName", true))
        .doesNotThrowAnyException();
  }
}
