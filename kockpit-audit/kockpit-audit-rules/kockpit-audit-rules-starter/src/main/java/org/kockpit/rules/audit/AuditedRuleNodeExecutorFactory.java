package org.kockpit.rules.audit;

import org.kockpit.audit.annotation.AuditAttributesAnnotationProcessor;
import org.kockpit.audit.api.AuditorEventService;
import org.kockpit.audit.api.AuditorKeyValueService;
import org.kockpit.rules.audit.flow.serializer.FlowExecutionAuditReportSerializer;
import org.kockpit.rules.executor.DefaultKEngineRuleNodeExecutorFactory;
import org.kockpit.rules.executor.RuleNodeExecutor;

public class AuditedRuleNodeExecutorFactory extends DefaultKEngineRuleNodeExecutorFactory {

  public static final String AUDITTYPE_KENGINE_FLOWS = "kengine.flows";
  private final AuditAttributesAnnotationProcessor auditAttributesAnnotationProcessor;
  private final AuditorEventService auditorEvents;
  private final AuditorKeyValueService auditorKeyValues;
  private final FlowExecutionAuditReportSerializer flowExecutionAuditReportSerializer;

  public AuditedRuleNodeExecutorFactory(
      AuditAttributesAnnotationProcessor auditAttributesAnnotationProcessor,
      AuditorEventService auditorEvents,
      AuditorKeyValueService auditorKeyValues,
      FlowExecutionAuditReportSerializer flowExecutionAuditReportSerializer
  ) {
    super();
    this.auditAttributesAnnotationProcessor = auditAttributesAnnotationProcessor;
    this.auditorEvents = auditorEvents;
    this.auditorKeyValues = auditorKeyValues;
    this.flowExecutionAuditReportSerializer = flowExecutionAuditReportSerializer;
  }

  @Override
  public <T> RuleNodeExecutor<T> createRuleNodeExecutor() {
    return new AuditedRuleNodeExecutor<>(
        auditAttributesAnnotationProcessor,
        auditorEvents,
        auditorKeyValues,
            flowExecutionAuditReportSerializer
            );
  }

  @Override
  public <T> RuleNodeExecutor<T> createRuleNodeExecutor(boolean audit) {
    if (audit) {
      return this.createRuleNodeExecutor();
    }
    // No Audit
    return super.createRuleNodeExecutor();
  }

  @Override
  public <T> RuleNodeExecutor<T> createRuleNodeExecutor(String executionName) {
    return new AuditedRuleNodeExecutor<>(
            auditAttributesAnnotationProcessor,
            auditorEvents,
            auditorKeyValues,
            flowExecutionAuditReportSerializer,
            executionName);
  }

  @Override
  public <T> RuleNodeExecutor<T> createRuleNodeExecutor(String executionName, boolean audit) {
    // Audit
    if (audit) {
      return this.createRuleNodeExecutor(executionName);
    }
    // No audit
    return super.createRuleNodeExecutor(executionName, false);
  }
}