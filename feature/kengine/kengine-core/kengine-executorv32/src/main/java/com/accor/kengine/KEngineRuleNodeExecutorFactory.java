package com.accor.kengine;

/** Temp RuleNodeExecutor factory. It will be removed in KEngine 4.0. */
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
