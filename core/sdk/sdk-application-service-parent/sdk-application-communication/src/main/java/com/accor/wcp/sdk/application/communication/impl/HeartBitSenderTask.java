package com.accor.wcp.sdk.application.communication.impl;

import com.accor.wcp.aws.kinesis.producer.AbstractKinesisProducer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

/** Heartbit(=hearbeat) scheduled task through Spring scheduler. */
@Slf4j
class HeartBitSenderTask extends AbstractKinesisProducer {

  private final CommunicationConfigurationProperties communicationConfigurationProperties;

  private final HeartBitProducer heartBitProducer;

  HeartBitSenderTask(
      HeartBitProducer heartBitProducer,
      CommunicationConfigurationProperties communicationConfigurationProperties) {
    this.heartBitProducer = heartBitProducer;
    this.communicationConfigurationProperties = communicationConfigurationProperties;
  }

  @PostConstruct
  void initHeartBitSender() {
    log.info("WCP SDK - Start sending HeartBeat (#heartbit)");
    CompletableFuture.runAsync(this::produce);
  }

  @PreDestroy
  void destroy() {
    heartBitProducer.sendHeartBit(true);
  }

  public void produce() {
    Thread.currentThread().setName("wcpsdk-comm-heartbeat");
    try {
      while (true) {
        heartBitProducer.sendHeartBit(false);
        Thread.sleep(communicationConfigurationProperties.getHeartbitIntervalMs());
      }
    } catch (InterruptedException e) {
      log.info("Interrupting sending HeartBit", e);
    }
  }
}
