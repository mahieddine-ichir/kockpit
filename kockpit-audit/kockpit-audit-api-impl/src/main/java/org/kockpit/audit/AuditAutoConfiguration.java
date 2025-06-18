package org.kockpit.audit;

import org.kockpit.audit.api.AuditModuleIntegration;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.kockpit.audit.api.AuditorService;
import org.kockpit.audit.obfuscate.AuditObfuscationSettings;
import org.kockpit.sdk.SdkApplicationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@Configuration
@EnableConfigurationProperties({AuditObfuscationSettings.class, SdkApplicationProperties.class})
@EnableScheduling
class AuditAutoConfiguration {

  @Bean
  AuditPostProcessor auditPostProcessor(
      List<AuditModuleIntegration> auditModuleIntegrations,
      AuditObfuscationService auditObfuscationServiceImpl) {
    return new AuditPostProcessor(auditModuleIntegrations, auditObfuscationServiceImpl);
  }

  @Bean
  public AuditorService auditor(
          SdkApplicationProperties sdkApplicationProperties,
          BuildProperties buildProperties,
          NotificationAuditReportManager notificationAuditReportManager,
          @Value("${kockpit.sdk.service.audit.ttl_ms:5}") Integer defaultAuditTtl) {
    return new KockpitAuditor(
        sdkApplicationProperties.getDomain(),
        sdkApplicationProperties.getEnv(),
        sdkApplicationProperties.getAppId(),
        buildProperties,
        notificationAuditReportManager,
        defaultAuditTtl);
  }

  @Bean
  public NotificationAuditReportManager notificationAuditReportManager(
      AuditPostProcessor auditPostProcessor,
      List<AuditReportNotificationService> auditReportNotificationServices,
      @Value("${kockpit.sdk.service.audit.notification.async:true}") boolean async,
      @Value("${kockpit.sdk.service.audit.notification.buffer.size:1000}") int bufferSize,
      @Value("${kockpit.sdk.service.audit.notification.buffer.partition-size:10}") int partitionSize,
      @Value("${kockpit.sdk.service.audit.notification.buffer.block:false}") boolean blockIfFullBuffer) {
    if (auditReportNotificationServices.isEmpty()) {
      throw new IllegalArgumentException(
          "No AuditReportNotificationService has been configured. "
              + "Please add one to the application context.");
    }
    return new NotificationAuditReportManager(
        async,
        auditReportNotificationServices,
        bufferSize,
        partitionSize,
        blockIfFullBuffer,
        auditPostProcessor);
  }

  @Bean
  public WcpAuditorEvent wcpAuditorEvent() {
    return new WcpAuditorEvent();
  }

  @Bean
  public WcpAuditorKeyValue wcpAuditorKeyValue() {
    return new WcpAuditorKeyValue();
  }
}
