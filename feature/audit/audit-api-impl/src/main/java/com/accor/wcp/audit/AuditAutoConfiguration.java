package com.accor.wcp.audit;

import com.accor.wcp.audit.obfuscate.AuditObfuscationSettings;
import com.accor.wcp.sdk.application.SdkApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties({AuditObfuscationSettings.class})
class AuditAutoConfiguration {

  @Bean
  AuditPostProcessor auditPostProcessor(
      List<AuditModuleIntegration> auditModuleIntegrations,
      AuditObfuscationService auditObfuscationServiceImpl) {
    return new AuditPostProcessor(auditModuleIntegrations, auditObfuscationServiceImpl);
  }

  @Bean
  public AuditorService wcpAuditor(
      SdkApplicationProperties sdkApplicationProperties,
      BuildProperties buildProperties,
      NotificationAuditReportManager notificationAuditReportManager,
      @Value("${wcp.sdk.service.audit.ttl.default}") Integer defaultAuditTtl) {
    return new WcpAuditor(
        sdkApplicationProperties.getDomain(),
        sdkApplicationProperties.getApplicationEnv(),
        sdkApplicationProperties.getApplicationId(),
        buildProperties,
        notificationAuditReportManager,
        defaultAuditTtl);
  }

  @Bean
  public NotificationAuditReportManager notificationAuditReportManager(
      AuditPostProcessor auditPostProcessor,
      @Autowired(required = false) AuditReportNotificationService auditReportNotificationService,
      @Value("${wcp.sdk.service.audit.notification.async:true}") boolean async,
      @Value("${wcp.sdk.service.audit.notification.buffer.size:1000}") int bufferSize,
      @Value("${wcp.sdk.service.audit.notification.buffer.size_threshold:500}") int bufferSizeThreshold,
      @Value("${wcp.sdk.service.audit.notification.buffer.partition-size:10}") int partitionSize,
      @Value("${wcp.sdk.service.audit.notification.buffer.block:false}") boolean blockIfFullBuffer,
      @Value("${wcp.sdk.service.audit.notification.silent-error:true}") boolean silentErrorProcessing) {
    return new NotificationAuditReportManager(
            async,
            auditReportNotificationService,
            bufferSize, bufferSizeThreshold,
            partitionSize,
            blockIfFullBuffer,
            silentErrorProcessing,
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
