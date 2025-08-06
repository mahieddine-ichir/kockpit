package com.accor.wcp.audit.module.autoconfigure.kengine;

import com.accor.kengine.DetailHandler;
import com.accor.kengine.registry.RuleNodeRegistry;
import com.accor.wcp.audit.AuditorEventService;
import com.accor.wcp.audit.AuditorKeyValueService;
import com.accor.wcp.audit.annotation.AuditAttributesAnnotationProcessor;
import com.accor.wcp.audit.module.kengine.AuditedRuleNodeExecutorFactory;
import com.accor.wcp.audit.module.kengine.KEngineFlowsAuditModuleActivator;
import com.accor.wcp.audit.module.kengine.flow.serializer.ExecutionEDTDTOConverter;
import com.accor.wcp.audit.module.kengine.flow.serializer.FlowExecutionAuditReportSerializer;
import com.accor.wcp.sdk.application.SdkApplicationProperties;
import com.accor.wcp.sdk.application.communication.App2WCPConsoleCommunicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
class KEngineAuditExtensionAutoConfiguration {

  @Bean
  @Primary
  @ConditionalOnProperty(
      prefix = "wcp.sdk.service.audit.kengine",
      name = "disabled",
      havingValue = "false",
      matchIfMissing = true)
  AuditedRuleNodeExecutorFactory auditedRuleNodeExecutorFactory(
      AuditAttributesAnnotationProcessor auditAttributesAnnotationProcessor,
      AuditorEventService auditorEventService,
      AuditorKeyValueService auditorKeyValueService,
      RuleNodeRegistry registry,
      DetailHandler detailHandler,
      SdkApplicationProperties sdkApplicationProperties) {
    return new AuditedRuleNodeExecutorFactory(
        auditAttributesAnnotationProcessor,
        auditorEventService,
        auditorKeyValueService,
        new FlowExecutionAuditReportSerializer(
            registry, detailHandler, new ExecutionEDTDTOConverter(sdkApplicationProperties)));
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "wcp.sdk.service.audit.kengine",
      name = "disabled",
      havingValue = "false",
      matchIfMissing = true)
  KEngineFlowsAuditModuleActivator kEngineFlowsAuditModuleActivator(
      RuleNodeRegistry registry,
      App2WCPConsoleCommunicationService app2WCPConsoleCommunicationService) {
    return new KEngineFlowsAuditModuleActivator(registry, app2WCPConsoleCommunicationService);
  }
}
