package org.kockpit.rules.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.annotation.AuditAttributesAnnotationProcessor;
import org.kockpit.audit.api.AuditorEventService;
import org.kockpit.audit.api.AuditorKeyValueService;
import org.kockpit.audit.api.AuditorService;
import org.kockpit.rules.RuleExecutionException;
import org.kockpit.rules.RuleNode;
import org.kockpit.rules.audit.flow.FlowExecutionAuditEvent;
import org.kockpit.rules.audit.flow.serializer.FlowExecutionAuditReportSerializer;
import org.kockpit.rules.execution.ExecutionResult;
import org.kockpit.rules.executor.RuleNodeExecutor;

import java.util.List;

import static org.kockpit.rules.audit.AuditedRuleNodeExecutorFactory.AUDITTYPE_KENGINE_FLOWS;
import static org.kockpit.rules.audit.flow.FlowExecutionKeyValues.getIndexedKeyValues;

@Slf4j
@RequiredArgsConstructor
class AuditedRuleNodeExecutor<T> extends RuleNodeExecutor<T> {

    private final AuditAttributesAnnotationProcessor auditAttributesAnnotationProcessor;
    private final AuditorService auditorService;
    private final AuditorEventService auditorEvents;
    private final AuditorKeyValueService auditorKeyValues;
    private final FlowExecutionAuditReportSerializer flowExecutionAuditReportSerializer;

    @Override
    public ExecutionResult execute(List<RuleNode<T>> ruleNodes, T context) {
        boolean auditStartedHere = false;
        if (! auditorService.isAuditStarted()) {
            auditorService.startAudit();
            auditStartedHere = true;
        }
      ExecutionResult executionResult = null;
      try {
        executionResult = super.execute(ruleNodes, context);
        return executionResult;
      } catch (RuleExecutionException e) {
        executionResult = e.getExecutionResult();
        throw e;
      } finally {
        audit(context, executionResult);
        if (auditStartedHere) {
            auditorService.stopAudit();
        }
      }
    }

    private void audit(Object context, ExecutionResult executionResult) {
      auditorEvents.addAuditEvents(
              AUDITTYPE_KENGINE_FLOWS, () -> List.of(computeFlowExecutionAuditEvent(executionResult)));
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

      // todo fixe these
      //flowExecutionAuditEvent.setExecutionName(executionName);
      /*
      flowExecutionAuditEvent.setExecutionName(getExecutionName());
      flowExecutionAuditEvent.setCreationTimestamp(getCreationTimestamp());
      flowExecutionAuditEvent.setPosition(getPosition());
       */
      return flowExecutionAuditEvent;
    }
  }