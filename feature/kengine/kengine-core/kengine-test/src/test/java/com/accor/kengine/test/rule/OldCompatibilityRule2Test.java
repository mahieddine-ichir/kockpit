package com.accor.kengine.test.rule;

import static org.assertj.core.api.Assertions.assertThat;

import com.accor.kengine.execution.ExecutionResult;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class OldCompatibilityRule2Test {
  @Test
  void should_execute_normally() {
    OldCompatibilityRule2 underTest = new OldCompatibilityRule2();

    Boolean context = true;
    ExecutionResult executionResult = FlowTestUtils.runFlow(context, Collections.singletonList(underTest));

    assertThat(executionResult.isSuccessful()).isTrue();
  }
}
