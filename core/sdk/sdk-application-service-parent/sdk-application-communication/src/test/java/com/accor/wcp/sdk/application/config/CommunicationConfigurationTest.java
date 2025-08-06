package com.accor.wcp.sdk.application.config;

import static org.mockito.Mockito.when;

import com.accor.wcp.sdk.application.SdkApplicationProperties;
import com.accor.wcp.sdk.application.SdkConfig;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CommunicationConfigurationTest {

  @Test
  void should_read_sdk_config_classpath_file() throws IOException {
    // Given
    SdkApplicationProperties sdkApplicationProperties = Mockito.mock(SdkApplicationProperties.class);
    when(sdkApplicationProperties.getWcpEnv()).thenReturn("dev");
    ApplicationSdkConfigurationPropertiesConfig communicationConfiguration = new ApplicationSdkConfigurationPropertiesConfig();
    communicationConfiguration.sdkConfigurationClasspathFile = "./sdk-config.yml";

    // When
    SdkConfig sdkConfig = communicationConfiguration.sdkConfig();

    // Then
    Assertions.assertNotNull(sdkConfig);
  }
}
