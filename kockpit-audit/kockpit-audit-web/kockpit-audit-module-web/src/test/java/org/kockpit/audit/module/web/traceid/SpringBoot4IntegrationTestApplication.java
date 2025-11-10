package org.kockpit.audit.module.web.traceid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

@Slf4j
@SpringBootApplication
class SpringBoot4IntegrationTestApplication {

  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(SpringBoot4IntegrationTestApplication.class);
    app.run(args);
  }

  @Bean
  BuildProperties buildProperties () {
      return new BuildProperties(new Properties());
  }
}
