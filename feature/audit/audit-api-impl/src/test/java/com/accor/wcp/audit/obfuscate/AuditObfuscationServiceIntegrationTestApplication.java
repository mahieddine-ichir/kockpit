package com.accor.wcp.audit.obfuscate;

import com.accor.wcp.sdk.application.SdkApplicationProperties;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AuditObfuscationServiceIntegrationTestApplication {

  @Bean
  SdkApplicationProperties sdkApplicationProperties() {
    // Fake
    return Mockito.mock(SdkApplicationProperties.class);
  }
}
