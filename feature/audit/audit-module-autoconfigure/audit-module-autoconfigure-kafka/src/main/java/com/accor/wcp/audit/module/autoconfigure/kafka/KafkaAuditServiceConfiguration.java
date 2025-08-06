package com.accor.wcp.audit.module.autoconfigure.kafka;

import com.accor.wcp.audit.module.kafka.KafkaAuditInterceptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class KafkaAuditServiceConfiguration {

  @Bean
  public InitializingBean kafkaAuditServiceInitializingContextBean(ApplicationContext context) {
    return () -> KafkaAuditInterceptor.setApplicationContext(context);
  }
}
