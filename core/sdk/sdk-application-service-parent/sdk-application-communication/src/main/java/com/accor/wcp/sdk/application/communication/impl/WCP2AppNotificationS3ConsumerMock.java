package com.accor.wcp.sdk.application.communication.impl;

import com.accor.wcp.sdk.application.SdkApplicationProperties;

/** Wcp2App Mock implementation for local/test env. */
class WCP2AppNotificationS3ConsumerMock extends WCP2AppNotificationS3Consumer {
  WCP2AppNotificationS3ConsumerMock(
      CommunicationConfigurationProperties communicationConfigurationProperties,
      WCP2AppNotificationProcessor processor,
      SdkApplicationProperties sdkApplicationProperties) {
    super(communicationConfigurationProperties, sdkApplicationProperties, processor);
  }

  @Override
  void init() {
    // Mock => no init
  }
}
