package org.kockpit.audit.notification.eventhub;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

@AutoConfiguration
@ConditionalOnProperty(
        value = "kockpit.audit.eventhub.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Slf4j
public class EventHubAuditServiceAutoConfiguration {

  @Bean
  AuditReportNotificationService eventHubAuditReportNotificationService(
          @Qualifier("eventHubProducerClient") EventHubProducerClient producerClient
  ) {
      log.info("➡️ Event Hub name: {}", producerClient.getEventHubName());
      return new EventHubAuditReportNotificationService(producerClient);
  }

  /**
   * With a connection string (SAS key), it is used as is; otherwise the client authenticates
   * against the namespace with DefaultAzureCredential (managed identity, workload identity, az login...).
   */
  @Bean(name = "eventHubProducerClient", destroyMethod = "close")
  EventHubProducerClient eventHubProducerClient(
          @Value("${kockpit.audit.notification.eventhub.event_hub_name}") String eventHubName,
          @Value("${kockpit.audit.notification.eventhub.connection_string:}") Optional<String> connectionString,
          @Value("${kockpit.audit.notification.eventhub.fully_qualified_namespace:}") Optional<String> fullyQualifiedNamespace
  ) {
      EventHubClientBuilder builder = new EventHubClientBuilder();
      connectionString
              .map(String::trim)
              .filter(StringUtils::isNotEmpty)
              .ifPresentOrElse(
                      s -> {
                          log.info("➡️ Initialize Event Hub client using a connection string");
                          builder.connectionString(s, eventHubName);
                      },
                      () -> {
                          String namespace = fullyQualifiedNamespace
                                  .map(String::trim)
                                  .filter(StringUtils::isNotEmpty)
                                  .orElseThrow(() -> new IllegalStateException(
                                          "kockpit.audit.notification.eventhub.connection_string or "
                                                  + "kockpit.audit.notification.eventhub.fully_qualified_namespace is required"));
                          log.info("➡️ Initialize Event Hub client on {} using Azure credentials", namespace);
                          builder.credential(namespace, eventHubName, new DefaultAzureCredentialBuilder().build());
                      });
      return builder.buildProducerClient();
  }
}
