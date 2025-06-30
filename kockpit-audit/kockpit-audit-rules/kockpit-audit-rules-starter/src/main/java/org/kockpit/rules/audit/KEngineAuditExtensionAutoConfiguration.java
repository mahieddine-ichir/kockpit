package org.kockpit.rules.audit;

import org.kockpit.audit.annotation.AuditAttributesAnnotationProcessor;
import org.kockpit.audit.api.AuditorEventService;
import org.kockpit.audit.api.AuditorKeyValueService;
import org.kockpit.rules.DetailHandler;
import org.kockpit.rules.audit.flow.serializer.ExecutionEDTDTOConverter;
import org.kockpit.rules.audit.flow.serializer.FlowExecutionAuditReportSerializer;
import org.kockpit.rules.registry.RuleNodeRegistry;
import org.kockpit.sdk.SdkApplicationProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@AutoConfiguration
class KEngineAuditExtensionAutoConfiguration {

  @Bean
  @Primary
  @ConditionalOnProperty(
      prefix = "kockpit.sdk.service.audit.rules",
      name = "disabled",
      havingValue = "false",
      matchIfMissing = true)
  AuditedRuleNodeExecutorFactory auditedRuleNodeExecutorFactory(
      AuditAttributesAnnotationProcessor auditAttributesAnnotationProcessor,
      AuditorEventService auditorEventService,
      AuditorKeyValueService auditorKeyValueService,
      RuleNodeRegistry<?> registry,
      DetailHandler detailHandler,
      SdkApplicationProperties sdkApplicationProperties) {
    return new AuditedRuleNodeExecutorFactory(
        auditAttributesAnnotationProcessor,
        auditorEventService,
        auditorKeyValueService,
        new FlowExecutionAuditReportSerializer(
            registry, detailHandler, new ExecutionEDTDTOConverter(sdkApplicationProperties)
        )
    );
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "kockpit.sdk.service.audit.rules",
      name = "disabled",
      havingValue = "false",
      matchIfMissing = true)
  KEngineFlowsAuditModuleActivator kEngineFlowsAuditModuleActivator(
      RuleNodeRegistry<?> registry
          // fixme , AppConsoleCommunicationService appConsoleCommunicationService
  ) {
  // fixme  return new KEngineFlowsAuditModuleActivator(registry, appConsoleCommunicationService);
    return new KEngineFlowsAuditModuleActivator(registry);
  }
}
