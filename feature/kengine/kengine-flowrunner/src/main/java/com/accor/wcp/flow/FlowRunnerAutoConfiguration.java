package com.accor.wcp.flow;

import com.accor.kengine.registry.RuleNodeRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlowRunnerAutoConfiguration {

  @Bean
  public FlowRunner flowRunner(
      RuleNodeRegistry<?> ruleNodeRegistry, RuleNodeExecutorFactory ruleNodeExecutorFactory) {
    return new FlowRunnerImpl(
        ruleNodeRegistry, ruleNodeExecutorFactory, kEngineFlowRunnerExecutionHandler());
  }

  @Bean
  DefaultKEngineFlowRunnerExecutionHandler kEngineFlowRunnerExecutionHandler() {
    return new DefaultKEngineFlowRunnerExecutionHandler();
  }
}
