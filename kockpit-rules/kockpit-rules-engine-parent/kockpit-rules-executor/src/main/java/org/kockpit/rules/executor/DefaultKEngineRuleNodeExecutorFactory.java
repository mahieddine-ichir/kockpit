package org.kockpit.rules.executor;

public class DefaultKEngineRuleNodeExecutorFactory implements KEngineRuleNodeExecutorFactory {

  @Override
  public <T> RuleNodeExecutor<T> createRuleNodeExecutor() {
    return new RuleNodeExecutor<>();
  }
}
