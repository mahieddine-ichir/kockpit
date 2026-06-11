package org.kockpit.audit.annotation;

import org.springframework.boot.autoconfigure.AutoConfiguration;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
public class AuditAnnotationConfig {

  @Bean
  AuditAttributesAnnotationProcessor auditAttributesAnnotationProcessor() {
    return new AuditAttributesAnnotationProcessor();
  }

  @Bean
  AuditCommandLineRunnerAspect auditCommandLineRunnerAspect(
      Environment environment, ConfigurableApplicationContext context) {
    return new AuditCommandLineRunnerAspect(environment, context);
  }
}
