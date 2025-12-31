package org.kockpit.audit.autoconfigure.console;

import org.kockpit.audit.api.AuditReportNotificationService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class LogAuditServiceAutoConfiguration {

  @Bean
  AuditReportNotificationService logAuditReportNotificationService() {
    return new SimpleLogAuditReportNotificationService();
  }
}
