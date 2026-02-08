package org.kockpit.rules.audit;

import org.kockpit.audit.annotation.AuditAttributesAnnotationProcessor;
import org.kockpit.audit.api.AuditorEventService;
import org.kockpit.audit.api.AuditorKeyValueService;
import org.kockpit.audit.api.AuditorService;
import org.kockpit.rules.DetailHandler;
import org.kockpit.rules.audit.flow.serializer.ExecutionEDTDTOConverter;
import org.kockpit.rules.audit.flow.serializer.FlowExecutionAuditReportSerializer;
import org.kockpit.rules.registry.RuleNodeRegistry;
import org.kockpit.sdk.SdkApplicationProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(
    prefix = "kockpit.audit.rules",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
class KEngineAuditExtensionAutoConfiguration {

  @Bean
  AuditedRuleNodeExecutorFactory auditedRuleNodeExecutorFactory(
          FlowExecutionAuditReportSerializer flowExecutionAuditReportSerializer,
      AuditAttributesAnnotationProcessor auditAttributesAnnotationProcessor,
      AuditorService auditorService,
      AuditorEventService auditorEventService,
      AuditorKeyValueService auditorKeyValueService
  ) {
    return new AuditedRuleNodeExecutorFactory(
        auditAttributesAnnotationProcessor,
        auditorService,
        auditorEventService,
        auditorKeyValueService,
        flowExecutionAuditReportSerializer
    );
  }

  @Bean
  FlowExecutionAuditReportSerializer flowExecutionAuditReportSerializer(
      RuleNodeRegistry<?> registry,
      DetailHandler detailHandler,
      SdkApplicationProperties sdkApplicationProperties
  ) {
    return new FlowExecutionAuditReportSerializer(registry, new RuleEngineAudit(detailHandler), new ExecutionEDTDTOConverter(sdkApplicationProperties));
  }

  @Bean
  KEngineFlowsAuditModuleActivator kEngineFlowsAuditModuleActivator(RuleNodeRegistry<?> registry
          // fixme , AppConsoleCommunicationService appConsoleCommunicationService
  ) {
  // fixme  return new KEngineFlowsAuditModuleActivator(registry, appConsoleCommunicationService);
    return new KEngineFlowsAuditModuleActivator(registry);
  }
}
