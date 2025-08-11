package org.kockpit.rules.starter;

import org.kockpit.rules.DefaultDetailHandler;
import org.kockpit.rules.DetailHandler;
import org.kockpit.rules.executor.*;
import org.kockpit.rules.registry.RuleNodeRegistry;
import org.kockpit.rules.registry.RuleNodesBuilderSupport;
import org.kockpit.rules.registry.model.Flow;
import org.kockpit.rules.registry.seemless.SeamLessRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.Optional;

@AutoConfiguration
public class KEngineAutoConfiguration {

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
      List<Flow> flows) {
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
