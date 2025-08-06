package com.accor.kengine.registry.seamless.v32;

import com.accor.kengine.registry.RuleNodesBuilderSupport;
import com.accor.kengine.registry.seamless.SeamLessRegistry;
import java.util.List;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@SpringBootApplication
@Configuration
class SeamLessApplication {

  @Bean
  @Primary
  public SeamLessRegistry seamLessRegistry(
      ApplicationContext applicationContext,
      List<RuleNodesBuilderSupport> ruleNodesBuilderSupports,
      List<com.accor.kengine.registry.model.Flow> flows) {
    return new SeamLessRegistry(applicationContext, ruleNodesBuilderSupports, flows);
  }
}
