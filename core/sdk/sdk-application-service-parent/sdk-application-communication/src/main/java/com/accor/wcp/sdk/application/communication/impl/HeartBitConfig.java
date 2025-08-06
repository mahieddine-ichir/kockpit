package com.accor.wcp.sdk.application.communication.impl;

import com.accor.wcp.sdk.application.SdkApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@Configuration
@EnableScheduling
class HeartBitConfig {

  @Bean
  Object heartBitSenderTask(
      HeartBitProducer heartBitProducer,
      SdkApplicationProperties sdkApplicationProperties,
      CommunicationConfigurationProperties communicationConfigurationProperties) {
    if (sdkApplicationProperties.isCommunicationEnabled()) {
      if ("mock".equals(sdkApplicationProperties.getWcpEnv())) {
        return new HeartBitSenderTaskMock();
      }
      return new HeartBitSenderTask(heartBitProducer, communicationConfigurationProperties);
    } else {
      // Communication is disabled
      log.info("SDK Communication is disabled. Skipping heartbit.");
      return new Object();
    }
  }
}
