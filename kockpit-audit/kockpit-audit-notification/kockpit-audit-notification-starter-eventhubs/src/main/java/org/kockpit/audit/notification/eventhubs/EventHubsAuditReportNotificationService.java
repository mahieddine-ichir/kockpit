package org.kockpit.audit.notification.eventhubs;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import org.kockpit.audit.api.AuditReport.AuditJsonReport;
import org.kockpit.audit.api.AuditReportNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @ref https://learn.microsoft.com/en-us/azure/event-hubs/event-hubs-java-get-started-send?tabs=passwordless%2Croles-azure-portal#send-events
 */
@Slf4j
@RequiredArgsConstructor
public class EventHubsAuditReportNotificationService implements AuditReportNotificationService {

  private final EventHubProducerClient producerClient;

  @Override
  public void notify(List<AuditJsonReport> auditReports) {
    this.publishEvents(auditReports);
  }

  /**
   * @throws IllegalArgumentException if the EventData is bigger than the max batch size.
   */
  void publishEvents(List<AuditJsonReport> auditReports) {
    // create a token using the default Azure credential
//    DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
//            .authorityHost(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)
//            .build();

    // create a producer client

//    EventHubProducerClient producer = new EventHubClientBuilder()
//            .fullyQualifiedNamespace(namespaceName)
//            .eventHubName(eventHubName)
//            .credential(credential)
//            .buildProducerClient();

    // sample events in an array
    List<EventData> allEvents = auditReports.stream().map(auditJsonReport -> new EventData(auditJsonReport.getAuditJson()))
            .toList();

    log.trace("Publishing events to EventHubs {}", allEvents);

    producerClient.send(allEvents);
    //producerClient.close();
    //eh-ns.servicebus.windows.net

/*
    // create a batch
    EventDataBatch eventDataBatch = producerClient.createBatch();

    for (EventData eventData : allEvents) {
      // try to add the event from the array to the batch
      if (!eventDataBatch.tryAdd(eventData)) {
        // if the batch is full, send it and then create a new batch
        producer.send(eventDataBatch);
        eventDataBatch = producer.createBatch();

        // Try to add that event that couldn't fit before.
        if (!eventDataBatch.tryAdd(eventData)) {
          throw new IllegalArgumentException("Event is too large for an empty batch. Max size: "
                  + eventDataBatch.getMaxSizeInBytes());
        }
      }
    }
    // send the last batch of remaining events
    if (eventDataBatch.getCount() > 0) {
      producer.send(eventDataBatch);
    }
    producer.close();*/
  }
}
