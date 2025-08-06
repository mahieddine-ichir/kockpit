package com.accor.kengine.testers.json;

import com.accor.kengine.registry.seamless.SeamLessRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SeamLessJsonApplication {
  @Bean
  SeamLessRegistry seamLessRegistry(ApplicationContext applicationContext) {
    return new SeamLessRegistry(applicationContext);
  }
}
