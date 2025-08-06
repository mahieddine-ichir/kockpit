package com.accor.wcp.sdk.application.communication.impl;

import com.accor.wcp.sdk.application.SdkApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
class App2WCPConsoleCommunicationConfig {
  @Bean
  App2WCPConsoleCommunicationServiceImpl app2WcpNotificationConsumer(
      SdkApplicationProperties sdkApplicationProperties,
      CommunicationConfigurationProperties communicationConfigurationProperties,
      ApplicationInstanceData applicationInstanceData) {
    if (sdkApplicationProperties.isCommunicationEnabled()) {
      if ("mock".equals(sdkApplicationProperties.getWcpEnv())) {
        return new App2WCPConsoleCommunicationServiceMock(
            communicationConfigurationProperties,
            sdkApplicationProperties,
            applicationInstanceData);
      }
      return new App2WCPConsoleCommunicationServiceImpl(
          communicationConfigurationProperties, sdkApplicationProperties, applicationInstanceData);
    } else {
      // Communication is disabled
      log.info("SDK Communication is disabled. Skipping notification producer (with a mock).");
      // Return mock instead of null bean
      return new App2WCPConsoleCommunicationServiceMock(
          communicationConfigurationProperties, sdkApplicationProperties, applicationInstanceData);
    }
  }
}
