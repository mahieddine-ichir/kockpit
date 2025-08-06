package com.accor.wcp.audit.it;

import com.accor.wcp.sdk.application.SdkApplicationProperties;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AuditServiceIntegrationTestApplication {

  @Bean
  SdkApplicationProperties sdkApplicationProperties() {
    // Fake
    return Mockito.mock(SdkApplicationProperties.class);
  }

  @Bean
  LocalNotificationService localNotificationService() {
    return new LocalNotificationService();
  }

  @Bean
  FakeAuditModuleIntegration fakeAuditModuleIntegration() {
    return new FakeAuditModuleIntegration();
  }
}
