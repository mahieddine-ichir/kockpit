package org.kockpit.audit.stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.common.InitialPositionInStream;
import software.amazon.kinesis.common.InitialPositionInStreamExtended;
import software.amazon.kinesis.coordinator.Scheduler;
import software.amazon.kinesis.metrics.MetricsLevel;
import software.amazon.kinesis.retrieval.polling.PollingConfig;

@Component
@Slf4j
@RequiredArgsConstructor
class NotificationConsumerKinesis implements ApplicationListener<ApplicationStartedEvent> {

  private final ConsumerConfig consumerConfig;

  @Value("${consumer.poll.max-records:50}")
  private int maxRecordsPerPoll;

  @Value("${consumer.poll.idle-time-ms:300}")
  private int idleTimeBetweenReadsInMillis;

  @Value("${consumer.initial-position-in-stream:TRIM_HORIZON}")
  private String initialPositionInStreamString;

  @Override
  public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
    InitialPositionInStream initialPositionInStream = InitialPositionInStream.valueOf(initialPositionInStreamString);
    log.info("initialPositionInStream: {}", initialPositionInStream);

    ConfigsBuilder configsBuilder = consumerConfig.getConfigsBuilder();

    /*
     * The Scheduler is the entry point to the KCL. This instance is configured with defaults
     * provided by the ConfigsBuilder.
     */
    PollingConfig retrievalSpecificConfig =
        new PollingConfig(consumerConfig.getStreamName(), configsBuilder.kinesisClient());
    retrievalSpecificConfig.idleTimeBetweenReadsInMillis(idleTimeBetweenReadsInMillis);
    retrievalSpecificConfig.maxRecords(maxRecordsPerPoll);
    Scheduler scheduler =
        new Scheduler(
            configsBuilder.checkpointConfig(),
            configsBuilder.coordinatorConfig(),
            configsBuilder.leaseManagementConfig().failoverTimeMillis(10000),
            configsBuilder.lifecycleConfig(),
            configsBuilder.metricsConfig().metricsLevel(MetricsLevel.NONE),
            configsBuilder.processorConfig(),
            configsBuilder
                .retrievalConfig()
                .retrievalSpecificConfig(retrievalSpecificConfig)
                .maxListShardsRetryAttempts(5)
                .initialPositionInStreamExtended(InitialPositionInStreamExtended.newInitialPosition(initialPositionInStream)));

    Thread schedulerThread = new Thread(scheduler);
    schedulerThread.setDaemon(true);
    schedulerThread.start();
  }
}
