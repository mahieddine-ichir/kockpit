package com.accor.kengine.starter;

import com.accor.kengine.DefaultDetailHandler;
import com.accor.kengine.DetailHandler;
import com.accor.kengine.KEngineRuleNodeExecutorFactory;
import com.accor.kengine.executor.DefaultKEngineRuleNodeExecutorFactory;
import com.accor.kengine.executor.FlowServiceMethodHandler;
import com.accor.kengine.executor.KEngineFlowRunnerExecutionHandler;
import com.accor.kengine.executor.KEngineFlowRunnerImpl;
import com.accor.kengine.registry.RuleNodeRegistry;
import com.accor.kengine.registry.RuleNodesBuilderSupport;
import com.accor.kengine.registry.seamless.SeamLessRegistry;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

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
  FlowServiceMethodHandler flowServiceMethodHandler(@Lazy KEngineFlowRunnerImpl kEngineFlowRunner,
                                                    @Autowired(required = false)
                                                    Optional<KEngineFlowRunnerExecutionHandler> kEngineFlowRunnerExecutionHandler) {
    return new FlowServiceMethodHandler(kEngineFlowRunner, kEngineFlowRunnerExecutionHandler);
  }

  @Bean
  @ConditionalOnMissingBean(RuleNodeRegistry.class)
  SeamLessRegistry ruleNodeRegistry(
      ApplicationContext applicationContext,
      List<RuleNodesBuilderSupport> ruleNodesBuilderSupports,
      List<com.accor.kengine.registry.model.Flow> flows) {
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
