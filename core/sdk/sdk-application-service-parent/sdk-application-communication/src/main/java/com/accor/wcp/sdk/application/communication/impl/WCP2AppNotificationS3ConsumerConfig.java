package com.accor.wcp.sdk.application.communication.impl;

import com.accor.wcp.sdk.application.SdkApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
class WCP2AppNotificationS3ConsumerConfig {

  @Bean
  Object wcp2AppNotificationConsumer(
      SdkApplicationProperties sdkApplicationProperties,
      CommunicationConfigurationProperties communicationConfigurationProperties,
      WCP2AppNotificationProcessor processor) {
    if (sdkApplicationProperties.isCommunicationEnabled()) {
      if ("mock".equals(sdkApplicationProperties.getWcpEnv())) {
        return new WCP2AppNotificationS3ConsumerMock(
            communicationConfigurationProperties, processor, sdkApplicationProperties);
      }
      return new WCP2AppNotificationS3Consumer(
          communicationConfigurationProperties, sdkApplicationProperties, processor);
    } else {
      // Communication is disabled
      log.info("SDK Communication is disabled. Skipping notification consumer (mock instance).");
      return new Object();
    }
  }
}
