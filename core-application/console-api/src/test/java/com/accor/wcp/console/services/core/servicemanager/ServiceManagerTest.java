package com.accor.wcp.console.services.core.servicemanager;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
@ActiveProfiles({"testmanifest"})
@ExtendWith(MockitoExtension.class)
class ServiceManagerTest {
  @SpringBootApplication(exclude = OAuth2ResourceServerAutoConfiguration.class)
  @ComponentScan("com.accor.wcp.console.services.core")
  static class App {}

  @Autowired private ServiceManagerImpl serviceManager;

  @MockitoBean
  private S3Client s3Client;

  @Test
  void loadServiceManagerAndInit() {
    assertThat(serviceManager.getServiceActivators().size()).isEqualTo(1);
  }
}
