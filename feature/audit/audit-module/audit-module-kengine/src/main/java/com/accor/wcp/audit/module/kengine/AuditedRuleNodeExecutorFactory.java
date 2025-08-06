package com.accor.wcp.audit.module.kengine;

import static com.accor.wcp.audit.module.kengine.flow.FlowExecutionKeyValues.getIndexedKeyValues;
import static java.util.List.of;

import com.accor.kengine.RuleExecutionException;
import com.accor.kengine.RuleNode;
import com.accor.kengine.RuleNodeExecutor;
import com.accor.kengine.execution.ExecutionResult;
import com.accor.wcp.audit.AuditorEventService;
import com.accor.wcp.audit.AuditorKeyValueService;
import com.accor.wcp.audit.annotation.AuditAttributesAnnotationProcessor;
import com.accor.wcp.audit.module.kengine.flow.FlowExecutionAuditEvent;
import com.accor.wcp.audit.module.kengine.flow.serializer.FlowExecutionAuditReportSerializer;
import com.accor.wcp.flow.DefaultRuleNodeExecutorFactory;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

public class AuditedRuleNodeExecutorFactory extends DefaultRuleNodeExecutorFactory {

  public static final String AUDITTYPE_KENGINE_FLOWS = "kengine.flows";
  private final AuditAttributesAnnotationProcessor auditAttributesAnnotationProcessor;
  private final AuditorEventService auditorEvents;
  private final AuditorKeyValueService auditorKeyValues;
  private final FlowExecutionAuditReportSerializer flowExecutionAuditReportSerializer;

  public AuditedRuleNodeExecutorFactory(
      AuditAttributesAnnotationProcessor auditAttributesAnnotationProcessor,
      AuditorEventService auditorEvents,
      AuditorKeyValueService auditorKeyValues,
      FlowExecutionAuditReportSerializer flowExecutionAuditReportSerializer) {
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
        flowExecutionAuditReportSerializer);
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

  @Slf4j
  private static class AuditedRuleNodeExecutor<T> extends DefaultRuleNodeExecutor<T> {

    private final AuditAttributesAnnotationProcessor auditAttributesAnnotationProcessor;
    private final AuditorEventService auditorEvents;
    private final AuditorKeyValueService auditorKeyValues;

    private final FlowExecutionAuditReportSerializer flowExecutionAuditReportSerializer;

    public AuditedRuleNodeExecutor(
        AuditAttributesAnnotationProcessor auditAttributesAnnotationProcessor,
        AuditorEventService auditorEvents,
        AuditorKeyValueService auditorKeyValues,
        FlowExecutionAuditReportSerializer flowExecutionAuditReportSerializer) {
      super(null);
      this.auditAttributesAnnotationProcessor = auditAttributesAnnotationProcessor;
      this.auditorEvents = auditorEvents;
      this.auditorKeyValues = auditorKeyValues;
      this.flowExecutionAuditReportSerializer = flowExecutionAuditReportSerializer;
    }

    public AuditedRuleNodeExecutor(
        AuditAttributesAnnotationProcessor auditAttributesAnnotationProcessor,
        AuditorEventService auditorEvents,
        AuditorKeyValueService auditorKeyValues,
        FlowExecutionAuditReportSerializer flowExecutionAuditReportSerializer,
        String executionName) {
      super(executionName);
      this.auditAttributesAnnotationProcessor = auditAttributesAnnotationProcessor;
      this.auditorEvents = auditorEvents;
      this.auditorKeyValues = auditorKeyValues;
      this.flowExecutionAuditReportSerializer = flowExecutionAuditReportSerializer;
    }

    @Override
    public ExecutionResult execute(List<RuleNode<T>> ruleNodes, T context) {
      ExecutionResult executionResult = null;
      try {
        executionResult = super.execute(ruleNodes, context);
        return executionResult;
      } catch (RuleExecutionException e) {
        executionResult = e.getExecutionResult();
        throw e;
      } finally {
        audit(context, executionResult);
      }
    }

    private void audit(Object context, ExecutionResult executionResult) {
      auditorEvents.addAuditEvents(
          AUDITTYPE_KENGINE_FLOWS, () -> of(computeFlowExecutionAuditEvent(executionResult)));
      auditorKeyValues.addIndexedKeyValues(
          () -> getIndexedKeyValues(context, executionResult, auditAttributesAnnotationProcessor));
    }

    private FlowExecutionAuditEvent computeFlowExecutionAuditEvent(
        ExecutionResult executionResult) {
      FlowExecutionAuditEvent flowExecutionAuditEvent =
          flowExecutionAuditReportSerializer.serialize(executionResult);
      flowExecutionAuditEvent.setStartTime(executionResult.getStartTimestamp());
      flowExecutionAuditEvent.setStartTime(executionResult.getStartTimestamp());
      flowExecutionAuditEvent.setEndTime(executionResult.getEndTimestamp());
      flowExecutionAuditEvent.setExecutionName(getExecutionName());
      flowExecutionAuditEvent.setCreationTimestamp(getCreationTimestamp());
      flowExecutionAuditEvent.setPosition(getPosition());
      return flowExecutionAuditEvent;
    }
  }
}
