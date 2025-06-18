package com.kockpit.rules.starter;

import com.kockpit.rules.DefaultDetailHandler;
import com.kockpit.rules.DetailHandler;
import com.kockpit.rules.executor.*;
import com.kockpit.rules.registry.RuleNodeRegistry;
import com.kockpit.rules.registry.RuleNodesBuilderSupport;
import com.kockpit.rules.registry.seemless.SeamLessRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.Optional;

@Configuration
public class KEngineAutoConfiguration {

  KEngineAutoConfiguration() {
    // No usage
  }

  @Bean
  KEngineFlowRunnerImpl kEngineFlowRunner(
      RuleNodeRegistry registry,
      KEngineRuleNodeExecutorFactory KEngineRuleNodeExecutorFactory,
      @Autowired(required = false)
          Optional<KEngineFlowRunnerExecutionHandler> kEngineFlowRunnerExecutionHandler) {
    return new KEngineFlowRunnerImpl(
        registry, KEngineRuleNodeExecutorFactory, kEngineFlowRunnerExecutionHandler);
  }

  @Bean
  FlowServiceMethodHandler flowServiceMethodHandler(@Lazy KEngineFlowRunnerImpl kEngineFlowRunner) {
    return new FlowServiceMethodHandler(kEngineFlowRunner);
  }

  @Bean
  @ConditionalOnMissingBean(RuleNodeRegistry.class)
  SeamLessRegistry ruleNodeRegistry(
      ApplicationContext applicationContext,
      List<RuleNodesBuilderSupport> ruleNodesBuilderSupports,
      List<com.kockpit.rules.registry.model.Flow> flows) {
    return new SeamLessRegistry(applicationContext, ruleNodesBuilderSupports, flows);
  }

  @Bean
  @ConditionalOnMissingBean(DetailHandler.class)
  DetailHandler detailHandler() {
    return new DefaultDetailHandler();
  }

  @Bean
  @ConditionalOnMissingBean(KEngineRuleNodeExecutorFactory.class)
  KEngineRuleNodeExecutorFactory defaultRuleNodeExecutorFactory() {
    return new DefaultKEngineRuleNodeExecutorFactory();
  }
}
