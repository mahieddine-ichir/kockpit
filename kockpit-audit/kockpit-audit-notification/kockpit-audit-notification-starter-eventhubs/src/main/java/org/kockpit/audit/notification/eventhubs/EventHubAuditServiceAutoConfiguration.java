package org.kockpit.audit.notification.eventhubs;

import com.azure.messaging.eventhubs.EventHubProducerClient;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventHubAuditServiceAutoConfiguration {

  @ConditionalOnBean(EventHubProducerClient.class)
  @Bean
  AuditReportNotificationService eventHubAuditReportNotificationService(
          EventHubProducerClient producerClient) {
    return new EventHubsAuditReportNotificationService(producerClient);
  }

    @ConditionalOnMissingBean(EventHubProducerClient.class)
    @Bean
    AuditReportNotificationService mockAuditReportNotificationService() {
        return new MockAuditReportNotificationService();
    }
}
