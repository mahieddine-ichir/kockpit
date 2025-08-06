package com.accor.wcp.sdk.application.communication.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

/** Mock heatbit task implementation. */
@Slf4j
class HeartBitSenderTaskMock extends HeartBitSenderTask {

  HeartBitSenderTaskMock() {
    super(null, null);
  }

  @Scheduled(fixedDelayString = "#{communicationConfigurationProperties.heartbitIntervalMs}")
  public void produce() {
    log.debug("Mock - Sending heartbit");
  }
}
