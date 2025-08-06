package com.accor.wcp.flow;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import com.accor.kengine.WarningExecutionException;
import com.accor.kengine.execution.ExecutionResult;
import com.accor.wcp.flow.errors.ErrorCodeImpl;
import com.accor.wcp.flow.errors.FlowExecutionWarning;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class DefaultKEngineFlowRunnerExecutionHandlerTest {

  DefaultKEngineFlowRunnerExecutionHandler underTest =
      new DefaultKEngineFlowRunnerExecutionHandler();

  @Test
  void should_log_warnings() {
    // Given
    ExecutionResult executionResult = new ExecutionResult(emptyList());

    // When & Then
    assertThatCode(
            () ->
                underTest.logWarnings(
                    Arrays.asList(new WarningExecutionException("Simple warning")),
                    executionResult))
        .doesNotThrowAnyException();

    // When & Then
    assertThatCode(
            () ->
                underTest.logWarnings(
                    Arrays.asList(
                        new FlowExecutionWarning(new ErrorCodeImpl("Title", 0, null, null, null))),
                    executionResult))
        .doesNotThrowAnyException();
  }
}
