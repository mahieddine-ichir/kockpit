package com.accor.wcp.console.services.core.communication.app2wcp;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.common.InitialPositionInStream;
import software.amazon.kinesis.common.InitialPositionInStreamExtended;
import software.amazon.kinesis.coordinator.Scheduler;
import software.amazon.kinesis.retrieval.polling.PollingConfig;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnBean(App2WcpConsumerConfig.class)
class App2WcpConsumerKinesis implements ApplicationListener<ApplicationStartedEvent> {

  private final App2WcpConsumerConfig consumerConfig;
  private Thread schedulerThread;

  @Value("${app2wcp.consumer.poll.max-records:10}")
  private int maxRecordsPerPoll;

  @Value("${app2wcp.consumer.poll.idle-time-ms:1000}")
  private int idleTimeBetweenReadsInMillis;

  @Override
  public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
    ConfigsBuilder configsBuilder = consumerConfig.getConfigBuilder();

    /*
     * The Scheduler is the entry point to the KCL. This instance is configured with defaults
     * provided by the ConfigsBuilder.
     */
    PollingConfig retrievalSpecificConfig =
        new PollingConfig(consumerConfig.getStreamName(), configsBuilder.kinesisClient());
    retrievalSpecificConfig.setIdleTimeBetweenReadsInMillis(idleTimeBetweenReadsInMillis);
    retrievalSpecificConfig.maxRecords(maxRecordsPerPoll);
    Scheduler scheduler =
        new Scheduler(
            configsBuilder.checkpointConfig(),
            configsBuilder.coordinatorConfig(),
            configsBuilder.leaseManagementConfig(),
            configsBuilder.lifecycleConfig(),
            configsBuilder.metricsConfig(),
            configsBuilder.processorConfig(),
            configsBuilder
                .retrievalConfig()
                .retrievalSpecificConfig(retrievalSpecificConfig)
                .maxListShardsRetryAttempts(1000)
                .initialPositionInStreamExtended(
                    InitialPositionInStreamExtended.newInitialPosition(
                        InitialPositionInStream.LATEST))
                .retrievalSpecificConfig(
                    new PollingConfig(
                        consumerConfig.getStreamName(), consumerConfig.getKinesisClient())));

    schedulerThread = new Thread(scheduler);
    schedulerThread.setDaemon(true);
    schedulerThread.start();
  }

  @PreDestroy
  void destroy() {
    schedulerThread.interrupt();
  }
}
