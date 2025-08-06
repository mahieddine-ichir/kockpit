package com.accor.wcp.sdk.application.starter;

import com.accor.wcp.sdk.application.SdkApplicationProperties;
import java.time.Duration;
import lombok.Builder;
import lombok.Data;

/** Default SDK Application properties container implementation. */
@Data
@Builder
class DefaultSdkApplicationProperties implements SdkApplicationProperties {
  private String domain;
  private String applicationEnv;
  private String applicationId;
  private String applicationVersion;
  private String wcpEnv;
  private boolean communicationEnabled;
  private Duration initializationTimeout;
}
