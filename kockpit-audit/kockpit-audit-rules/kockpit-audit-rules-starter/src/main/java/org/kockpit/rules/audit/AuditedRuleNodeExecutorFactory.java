package org.kockpit.rules.audit;

import lombok.RequiredArgsConstructor;
import org.kockpit.audit.annotation.AuditAttributesAnnotationProcessor;
import org.kockpit.audit.api.AuditorEventService;
import org.kockpit.audit.api.AuditorKeyValueService;
import org.kockpit.audit.api.AuditorService;
import org.kockpit.rules.audit.flow.serializer.FlowExecutionAuditReportSerializer;
import org.kockpit.rules.executor.DefaultKEngineRuleNodeExecutorFactory;
import org.kockpit.rules.executor.RuleNodeExecutor;

@RequiredArgsConstructor
public class AuditedRuleNodeExecutorFactory extends DefaultKEngineRuleNodeExecutorFactory {

  public static final String AUDITTYPE_KENGINE_FLOWS = "kengine.flows";

  private final AuditAttributesAnnotationProcessor auditAttributesAnnotationProcessor;
  private final AuditorService auditorService;
  private final AuditorEventService auditorEvents;
  private final AuditorKeyValueService auditorKeyValues;
  private final FlowExecutionAuditReportSerializer flowExecutionAuditReportSerializer;

  @Override
  public <T> RuleNodeExecutor<T> createRuleNodeExecutor() {
    return new AuditedRuleNodeExecutor<>(
            auditAttributesAnnotationProcessor,
            auditorService,
            auditorEvents,
            auditorKeyValues,
            flowExecutionAuditReportSerializer
    );
  }

  @Override
  public <T> RuleNodeExecutor<T> createRuleNodeExecutor(String executionName) {
    return new AuditedRuleNodeExecutor<>(
            auditAttributesAnnotationProcessor,
            auditorService,
            auditorEvents,
            auditorKeyValues,
            flowExecutionAuditReportSerializer
    );
  }

  @Override
  public <T> RuleNodeExecutor<T> createRuleNodeExecutor(String executionName, boolean audit) {
    // Audit
    if (audit) {
      return this.createRuleNodeExecutor(executionName);
    } else {
      // No audit
      return super.createRuleNodeExecutor(executionName, false);
    }
  }
}