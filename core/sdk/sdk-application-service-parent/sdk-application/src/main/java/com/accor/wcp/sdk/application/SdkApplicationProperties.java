package com.accor.wcp.sdk.application;

import java.time.Duration;

/** SDK configuration properties container. */
public interface SdkApplicationProperties {
  String getDomain();

  String getApplicationEnv();

  String getApplicationId();

  String getApplicationVersion();

  /** @return targeted WCP environment */
  String getWcpEnv();

  /** @return flag that tell if communication is enabled or not */
  boolean isCommunicationEnabled();

  /** @return timeout duration for service initialization. */
  Duration getInitializationTimeout();
}
