package org.kockpit.audit.stream.kinesis;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.coordinator.Scheduler;

@Slf4j
@RequiredArgsConstructor
public class KinesisStreamListener {

    private final KinesisAsyncClient kinesisClient;
    private final DynamoDbAsyncClient dynamoDbClient;
    private final CloudWatchAsyncClient cloudWatchClient;
    private final KclRecordProcessorFactory recordProcessorFactory;
    private final String streamName;
    private final String applicationName;
    private final String workerIdentifier;

    private Scheduler scheduler;
    
    @Async  // Runs in separate thread
    public void startAsync() {
        log.info("✅ Starting KCL consumer for stream: {} with application: {}", streamName, applicationName);

        try {
            // KCL 3.x API
            ConfigsBuilder configsBuilder = new ConfigsBuilder(
                streamName,
                applicationName,
                kinesisClient,
                dynamoDbClient,
                cloudWatchClient,
                workerIdentifier,
                recordProcessorFactory
            );

            scheduler = new Scheduler(
                configsBuilder.checkpointConfig(),
                configsBuilder.coordinatorConfig(),
                configsBuilder.leaseManagementConfig(),
                configsBuilder.lifecycleConfig(),
                configsBuilder.metricsConfig(),
                configsBuilder.processorConfig(),
                configsBuilder.retrievalConfig()
            );

            log.info("✅ KCL Scheduler configured for worker: {}", workerIdentifier);
            scheduler.run();

        } catch (Exception e) {
            log.error("❌ KCL Scheduler error: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void stop() {
        if (scheduler != null) {
            log.info("⏹️ Shutting down KCL Scheduler...");
            scheduler.shutdown();
            log.info("⏹️ KCL Scheduler stopped");
        }
    }
}
