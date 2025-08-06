package com.accor.wcp.audit.notification.autoconfigure.service;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.accor.wcp.audit.AuditModuleActivator;
import com.accor.wcp.audit.AuditReportNotificationService;
import com.accor.wcp.audit.SimpleLogAuditReportNotificationService;
import com.accor.wcp.audit.notification.autoconfigure.http.HttpAuditNotificationServiceConfiguration;
import com.accor.wcp.audit.notification.autoconfigure.kinesis.KinesisAuditNotificationServiceConfiguration;
import com.accor.wcp.sdk.application.SdkApplicationProperties;
import com.accor.wcp.sdk.application.config.SdkConfigurationAws;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationAuditServiceAutoConfiguration {
  @Value("${wcp.sdk.aws.kinesis.endpoint:http://localhost:4566}")
  private String kinesisEndpoint;

  @Value("${wcp.sdk.service.audit.notification.http.url:}")
  private String localAuditNotificationUrl;

  @Value("${wcp.sdk.service.audit.notification.log.enabled:false}")
  private boolean localAuditNotificationLogDebug;

  @Bean
  AuditApplicationServiceIntegration auditApplicationServiceIntegration(
      List<AuditModuleActivator> auditModuleActivators) {
    return new AuditApplicationServiceIntegration(auditModuleActivators);
  }

  @Bean
  AuditReportNotificationService auditReportNotificationService(
          SdkApplicationProperties sdkApplicationProperties, SdkConfigurationAws sdkConfigurationAws) {
    return switch (sdkApplicationProperties.getWcpEnv()) {
      case "none", "mock" -> buildMock();
      case "local" -> buildLocal();
      case "bch" -> buildBch(sdkApplicationProperties, sdkConfigurationAws);
      default -> buildForEnv(sdkApplicationProperties, sdkConfigurationAws);
    };
  }

  private AuditReportNotificationService buildBch(
          SdkApplicationProperties sdkApplicationProperties, SdkConfigurationAws sdkConfigurationAws) {
    return new KinesisAuditNotificationServiceConfiguration(
            "auditstream-" + sdkApplicationProperties.getWcpEnv(),
            "arn:aws:iam::" + sdkConfigurationAws.getAccountId() + ":role/AuditClientAppKinesisAssumeRole-" + sdkApplicationProperties.getWcpEnv(),
            null)
        .kinesisAuditNotificationService();
  }

  private AuditReportNotificationService buildForEnv(
          SdkApplicationProperties sdkApplicationProperties, SdkConfigurationAws sdkConfigurationAws) {
    return new KinesisAuditNotificationServiceConfiguration(
            "auditstream-" + sdkApplicationProperties.getWcpEnv(),
            "arn:aws:iam::" + sdkConfigurationAws.getAccountId() + ":role/AuditClientAppKinesisAssumeRole",
            null)
            .kinesisAuditNotificationService();
  }

  private AuditReportNotificationService buildLocal() {
    if (localAuditNotificationLogDebug) {
      return new SimpleLogAuditReportNotificationService();
    } else if (isNotEmpty(localAuditNotificationUrl)) {
      return new HttpAuditNotificationServiceConfiguration(localAuditNotificationUrl)
          .httpAuditNotificationService();
    } else {
      return new KinesisAuditNotificationServiceConfiguration(
              "auditstream-local", null, kinesisEndpoint)
          .kinesisAuditNotificationService();
    }
  }

  private AuditReportNotificationService buildMock() {
    return new SimpleLogAuditReportNotificationService();
  }
}
