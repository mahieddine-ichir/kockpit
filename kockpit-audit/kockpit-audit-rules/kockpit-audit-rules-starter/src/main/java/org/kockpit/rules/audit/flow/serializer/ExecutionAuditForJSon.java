package org.kockpit.rules.audit.flow.serializer;

import lombok.Data;
import org.kockpit.audit.rules.data.model.Execution;
import org.kockpit.audit.rules.data.model.ExecutionLog;
import org.kockpit.audit.rules.data.model.ResultStatus;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Data
public class ExecutionAuditForJSon implements Execution {

  private String executionUuid;

  private Date date;

  private long time;

  private ResultStatus resultStatus;

  private String detail;

  private String message;

  private Long registryId;

  private List<RuleExecutionAudit> rules;

  private List<SimpleExecutionLog> logs;

  public ExecutionAuditForJSon() {}

  public ExecutionAuditForJSon(Execution execution) {
    executionUuid = execution.getExecutionUuid();
    this.date = execution.getDate();
    this.time = execution.getTime();
    this.detail = execution.getDetail();
    this.message = execution.getMessage();
    this.resultStatus = execution.getResultStatus();
    this.registryId = execution.getRegistryId();
    this.rules = execution.getRules().stream().map(RuleExecutionAudit::new).collect(toList());
    this.logs =
        execution.getExecutionLogs().stream()
            .map(
                kengineLog ->
                    new SimpleExecutionLog(
                        kengineLog.getAction(), kengineLog.getLog(), kengineLog.getTs()))
            .collect(Collectors.toList());
  }

  @Override
  public List<? extends ExecutionLog> getExecutionLogs() {
    return logs;
  }
}
