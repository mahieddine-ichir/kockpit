package org.kockpit.audit.stream;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.kockpit.audit.stream.api.model.AuditReport;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import reactor.core.Disposable;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AuditConsuming implements CommandLineRunner {

    private final List<AuditConsumer> auditConsumers;

    private final EventHubConsumerAsyncClient eventHubConsumerAsyncClient;

    private final ObjectMapper objectMapper;

    @PostConstruct
    void startConsumers(
    ) {
        auditConsumers.forEach(AuditConsumer::start);
    }

    @PreDestroy
    void stopConsumers() {
        auditConsumers.forEach(AuditConsumer::stop);
    }

    @Override
    public void run(String... args) {

        log.info("""
                        Consumer properties:
                            namespace: {},
                            eventHubName: {},
                            consumerGroup: {}
                        """,
                eventHubConsumerAsyncClient.getFullyQualifiedNamespace(),
                eventHubConsumerAsyncClient.getEventHubName(),
                eventHubConsumerAsyncClient.getConsumerGroup()
        );

        EventPosition startingPosition = EventPosition.latest();
        eventHubConsumerAsyncClient.getPartitionIds().subscribe(partitionId -> {
            Disposable subscription = eventHubConsumerAsyncClient.receiveFromPartition(partitionId, startingPosition)
                    .subscribe(partitionEvent -> {
                        EventData event = partitionEvent.getData();
                        try {
                            AuditReport audit = objectMapper.readValue(event.getBody(), AuditReport.class);
                            auditConsumers.forEach(auditConsumer -> auditConsumer.accept(audit));
                        } catch (Exception e) {
                            log.error("Error deserializing event {}. Error: {}", event, e.getMessage(), e);
                        }

                        //PartitionContext partitionContext = partitionEvent.getPartitionContext();
                        //System.out.printf("Received event from partition '%s'%n", partitionContext.getPartitionId());
                        //System.out.printf("Contents of event as string: '%s'%n", event.getBodyAsString());

                    }, error -> auditConsumers.forEach(auditConsumer -> auditConsumer.onError(error)), () -> {
                        // This is a terminal signal.  No more events will be received from the same Flux object.
                        System.out.print("Stream has ended.");
                    });

            Runtime.getRuntime().addShutdownHook(new Thread(subscription::dispose));
        });
    }
}
