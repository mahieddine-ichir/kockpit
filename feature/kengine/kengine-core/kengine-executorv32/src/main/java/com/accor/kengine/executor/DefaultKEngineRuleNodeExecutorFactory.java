package com.accor.kengine.executor;

import com.accor.kengine.KEngineRuleNodeExecutorFactory;
import com.accor.kengine.RuleNodeExecutor;

public class DefaultKEngineRuleNodeExecutorFactory implements KEngineRuleNodeExecutorFactory {

  @Override
  public <T> RuleNodeExecutor<T> createRuleNodeExecutor() {
    return new RuleNodeExecutor<>();
  }
}
