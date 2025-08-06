package com.accor.wcp.console.services.core.application.heartbit;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@ConditionalOnBean(HeartBitConsumerConfig.class)
public class HeartBitConsumerKinesis implements ApplicationListener<ApplicationStartedEvent> {

  private final HeartBitConsumerConfig consumerConfig;
  private Thread schedulerThread;

  @Override
  public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
    ConfigsBuilder configsBuilder = consumerConfig.getConfigBuilder();

    /*
     * The Scheduler is the entry point to the KCL. This instance is configured with defaults
     * provided by the ConfigsBuilder.
     */
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
                .maxListShardsRetryAttempts(500)
                //                .streamTracker(new
                // SingleStreamTracker(consumerConfig.getStreamName()))
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
  private void destroy() {
    schedulerThread.interrupt();
  }
}
