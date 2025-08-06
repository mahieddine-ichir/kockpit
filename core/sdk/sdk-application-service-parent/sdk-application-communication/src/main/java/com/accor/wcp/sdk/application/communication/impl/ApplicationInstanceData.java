package com.accor.wcp.sdk.application.communication.impl;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import com.accor.wcp.sdk.application.SdkApplicationProperties;
import jakarta.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Internal application instance data model. This class is not exposed. */
@Component
@Getter
class ApplicationInstanceData {
  private final String domain;
  private final String env;
  private final String applicationId;

  private String applicationInstance;

  @Value("${sdk.app.manual.hostname:}")
  private String manualHostname;

  ApplicationInstanceData(SdkApplicationProperties sdkApplicationProperties) {
    this.domain = sdkApplicationProperties.getDomain();
    this.env = sdkApplicationProperties.getApplicationEnv();
    this.applicationId = sdkApplicationProperties.getApplicationId();
  }

  @PostConstruct
  void init() {
    // Special case for local dev or force hostname
    if (isNotEmpty(manualHostname)) {
      applicationInstance = manualHostname;
      return;
    }

    // Hostname
    try {
      applicationInstance = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      applicationInstance = "UNKNOWN-" + Math.random();
    }
  }
}
