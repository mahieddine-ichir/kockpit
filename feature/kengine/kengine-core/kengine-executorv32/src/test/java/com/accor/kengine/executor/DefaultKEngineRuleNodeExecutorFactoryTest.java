package com.accor.kengine.executor;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.accor.kengine.RuleNodeExecutor;
import org.junit.jupiter.api.Test;

class DefaultKEngineRuleNodeExecutorFactoryTest {

  @Test
  void createRuleNodeExecutor() {
    RuleNodeExecutor<Object> ruleNodeExecutor =
        new DefaultKEngineRuleNodeExecutorFactory().createRuleNodeExecutor();
    assertThat(ruleNodeExecutor).isNotNull();
  }
}
