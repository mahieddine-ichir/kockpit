package org.kockpit.audit;

import org.kockpit.audit.api.AuditModuleIntegration;
import org.kockpit.audit.api.AuditReportNotificationService;
import org.kockpit.audit.api.AuditorService;
import org.kockpit.audit.obfuscate.AuditObfuscationSettings;
import org.kockpit.sdk.SdkApplicationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@AutoConfiguration
@EnableConfigurationProperties({AuditObfuscationSettings.class, SdkApplicationProperties.class})
@EnableScheduling
class AuditAutoConfiguration {

  @Bean
  AuditPostProcessor auditPostProcessor(
      List<AuditModuleIntegration> auditModuleIntegrations,
      AuditObfuscationService auditObfuscationServiceImpl) {
    return new AuditPostProcessor(auditModuleIntegrations, auditObfuscationServiceImpl);
  }

  @ConditionalOnMissingBean(AuditReportNotificationService.class)
  @Bean
  AuditReportNotificationService auditReportNotificationService() {
    return new SimpleLogNotificationService();
  }

  @Bean
  AuditorService auditor(
          SdkApplicationProperties sdkApplicationProperties,
          BuildProperties buildProperties,
          NotificationAuditReportManager notificationAuditReportManager,
          @Value("${kockpit.audit.ttl_default_in_days:5}") Integer defaultAuditTtl) {
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
      List<AuditReportNotificationService> auditReportNotificationServices,
      @Value("${kockpit.audit.notification.async:true}") boolean async,
      @Value("${kockpit.audit.notification.buffer.block:false}") boolean blockIfFullBuffer,
      @Value("${kockpit.audit.notification.silent-error:true}") boolean silentErrorProcessing,
      AuditReportsQueueHandler auditReportsQueueHandler
  ) {
    if (auditReportNotificationServices.isEmpty()) {
      throw new IllegalArgumentException(
          "No AuditReportNotificationService has been configured. "
              + "Please add one to the application context.");
    }
    return new NotificationAuditReportManager(async, blockIfFullBuffer, silentErrorProcessing, auditReportsQueueHandler);
  }

  @Bean
  AuditReportsQueueHandler auditReportsQueueHandler(
          AuditPostProcessor auditPostProcessor,
          List<AuditReportNotificationService> auditReportNotificationServices,
          @Value("${kockpit.audit.notification.buffer.size:1000}") int bufferSize,
          @Value("${kockpit.audit.notification.buffer.threshold:300}") int bufferThreshold,
          @Value("${kockpit.audit.notification.buffer.partition-size:10}") int partitionSize
  ) {
    return new AuditReportsQueueHandler(auditPostProcessor, auditReportNotificationServices, partitionSize, bufferSize, bufferThreshold);
  }

  @Bean
  public KockpitAuditorEvent auditorEvent() {
    return new KockpitAuditorEvent();
  }

  @Bean
  public KockpitAuditorKeyValue auditorKeyValue() {
    return new KockpitAuditorKeyValue();
  }
}
