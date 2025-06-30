package org.kockpit.rules.executor;

public interface KEngineRuleNodeExecutorFactory {
  <T> RuleNodeExecutor<T> createRuleNodeExecutor();

  default <T> RuleNodeExecutor<T> createRuleNodeExecutor(String executionName) {
    return createRuleNodeExecutor();
  }

  default <T> RuleNodeExecutor<T> createRuleNodeExecutor(String executionName, boolean audit) {
    return createRuleNodeExecutor(executionName);
  }

  default <T> RuleNodeExecutor<T> createRuleNodeExecutor(boolean audit) {
    return createRuleNodeExecutor();
  }
}
