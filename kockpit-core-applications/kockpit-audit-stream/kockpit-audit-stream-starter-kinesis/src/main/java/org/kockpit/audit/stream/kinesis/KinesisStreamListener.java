package org.kockpit.audit.stream.kinesis;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.common.KinesisClientUtil;
import software.amazon.kinesis.coordinator.Scheduler;

@Component
@Slf4j
@RequiredArgsConstructor
public class KinesisStreamListener {

    private final KinesisClient kinesisClient;
    private final DynamoDbClient dynamoDbClient;
    private final CloudWatchClient cloudWatchClient;
    private final KclRecordProcessorFactory recordProcessorFactory;

    @Value("${kockpit.audit.stream.kinesis.streamName:auditstream-local}")
    private String streamName;

    @Value("${kockpit.audit.stream.kinesis.applicationName:audit-consumer}")
    private String applicationName;

    @Value("${kockpit.audit.stream.kinesis.workerIdentifier:#{T(java.util.UUID).randomUUID().toString()}}")
    private String workerIdentifier;

    private Scheduler scheduler;
    
    @Async  // Runs in separate thread
    public void startAsync() {
        log.info("✅ Starting KCL consumer for stream: {} with application: {}", streamName, applicationName);

        try {
            ConfigsBuilder configsBuilder = new ConfigsBuilder(
                streamName,
                applicationName,
                kinesisClient,
                dynamoDbClient,
                cloudWatchClient,
                workerIdentifier,
                recordProcessorFactory.shardRecordProcessor()
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
