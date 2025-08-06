package com.accor.wcp.sdk.application.communication.impl;

import com.accor.wcp.sdk.application.SdkApplicationProperties;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

/** App2Wcp Mock implementation (communication disabled or local mock testing configuration). */
@Slf4j
class App2WCPConsoleCommunicationServiceMock extends App2WCPConsoleCommunicationServiceImpl {
  App2WCPConsoleCommunicationServiceMock(
      CommunicationConfigurationProperties communicationConfigurationProperties,
      SdkApplicationProperties sdkApplicationProperties,
      ApplicationInstanceData applicationInstanceData) {
    super(communicationConfigurationProperties, sdkApplicationProperties, applicationInstanceData);
  }

  @Override
  String send(String streamName, ApplicationMessageNotificationDto message) {
    log.debug("Mock - Send to stream {}, message: {}", streamName, message);
    return UUID.randomUUID().toString();
  }
}
