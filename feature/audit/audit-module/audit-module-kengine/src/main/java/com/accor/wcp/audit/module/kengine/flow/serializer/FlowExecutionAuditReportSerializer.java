package com.accor.wcp.audit.module.kengine.flow.serializer;

import static java.util.Collections.emptyList;

import com.accor.kengine.DetailHandler;
import com.accor.kengine.audit.ExecutionAudit;
import com.accor.kengine.audit.RuleEngineAudit;
import com.accor.kengine.audit.model.Execution;
import com.accor.kengine.execution.ExecutionResult;
import com.accor.kengine.registry.RuleNodeRegistry;
import com.accor.wcp.audit.module.kengine.flow.FlowExecutionAuditEvent;

public class FlowExecutionAuditReportSerializer {

  private final RuleNodeRegistry<?> registry;
  private final RuleEngineAudit ruleEngineAudit;
  private final ExecutionEDTDTOConverter executionEDTDTOConverter;

  public FlowExecutionAuditReportSerializer(
      RuleNodeRegistry<?> registry,
      DetailHandler detailHandler,
      ExecutionEDTDTOConverter executionEDTDTOConverter) {
    this.registry = registry;
    ruleEngineAudit = new RuleEngineAudit(detailHandler);
    this.executionEDTDTOConverter = executionEDTDTOConverter;
  }

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
