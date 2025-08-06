package com.accor.kengine.registry.seamless.json;

import com.accor.kengine.registry.seamless.SeamLessRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
class SeamLessJsonApplication {
  @Bean
  SeamLessRegistry seamLessJsonRegistry(ApplicationContext applicationContext) {
    return new SeamLessRegistry(applicationContext);
  }
}
