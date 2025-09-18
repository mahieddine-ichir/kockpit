package org.kockpit.audit.notification.kafka;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@Slf4j
public class KafkaAuditServiceAutoConfiguration {

  @Bean
  AuditReportNotificationService eventHubAuditReportNotificationService(
          KafkaTemplate<String, String> kafkaTemplate,
          @Value("${kockpit.sdk.service.audit.notification.topic}") String topic) {
    return new KafkaAuditReportNotificationService(kafkaTemplate, topic);
  }
}
