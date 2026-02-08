package org.kockpit.rules.audit.flow.serializer;

import lombok.RequiredArgsConstructor;
import org.kockpit.audit.rules.data.model.Execution;
import org.kockpit.rules.audit.ExecutionAudit;
import org.kockpit.rules.audit.RuleEngineAudit;
import org.kockpit.rules.audit.flow.FlowExecutionAuditEvent;
import org.kockpit.rules.execution.ExecutionResult;
import org.kockpit.rules.registry.RuleNodeRegistry;

import static java.util.Collections.emptyList;

@RequiredArgsConstructor
public class FlowExecutionAuditReportSerializer {

  private final RuleNodeRegistry<?> registry;
  private final RuleEngineAudit ruleEngineAudit;
  private final ExecutionEDTDTOConverter executionEDTDTOConverter;

  public FlowExecutionAuditEvent serialize(ExecutionResult executionResult) {
    Execution execution = auditFlowExecution(executionResult);
    ExecutionEDTDTO executionEDTDTO = serializeExecution(execution);
    return FlowExecutionAuditEvent.builder().executionEDTDTO(executionEDTDTO).build();
  }

  private ExecutionEDTDTO serializeExecution(Execution execution) {
    return executionEDTDTOConverter.convert(execution);
  }

  private ExecutionAuditForJSon auditFlowExecution(ExecutionResult executionResult) {
    ExecutionAudit executionAudit = ruleEngineAudit.compute(executionResult);
    Long registryId = registry.getCurrentRegistry().getId();
    executionAudit.setRegistryId(registryId);
    executionAudit.setExecutionLogs(emptyList());
    return new ExecutionAuditForJSon(executionAudit);
  }
}
