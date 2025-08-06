package com.accor.wcp.audit.it;

import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
class SpringBoot4IntegrationTestApplication {

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(SpringBoot4IntegrationTestApplication.class);
    app.run(args);
  }

  @Bean
  @ConditionalOnMissingBean(BuildProperties.class)
  BuildProperties buildProperties() {
    return new BuildProperties(new Properties());
  }
}
