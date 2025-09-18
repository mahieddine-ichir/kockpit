package org.kockpit.audit.notification.eventhubs;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.api.AuditReport.AuditJsonReport;
import org.kockpit.audit.api.AuditReportNotificationService;

import java.util.List;

/**
 * @ref https://learn.microsoft.com/en-us/azure/event-hubs/event-hubs-java-get-started-send?tabs=passwordless%2Croles-azure-portal#send-events
 */
@Slf4j
@RequiredArgsConstructor
class EventHubsAuditReportNotificationService implements AuditReportNotificationService {

  private final EventHubProducerClient producerClient;

  @Override
  public void notify(List<AuditJsonReport> auditReports) {
    this.publishEvents(auditReports);
  }

  /**
   * @throws IllegalArgumentException if the EventData is bigger than the max batch size.
   */
  void publishEvents(List<AuditJsonReport> auditReports) {
    // sample events in an array
    List<EventData> allEvents = auditReports.stream().map(auditJsonReport -> new EventData(auditJsonReport.getAuditJson()))
            .toList();
//      long currentTimeMillis = System.currentTimeMillis();
      producerClient.send(allEvents);
//      System.out.println("Time to send " + allEvents.size() + " events: " + (System.currentTimeMillis() - currentTimeMillis));
  }
}
