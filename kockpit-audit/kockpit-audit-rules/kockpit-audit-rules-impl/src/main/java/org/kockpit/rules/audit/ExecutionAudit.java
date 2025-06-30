package org.kockpit.rules.audit;

import org.kockpit.rules.execution.ExecutionResult;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.kockpit.audit.rules.data.model.Execution;
import org.kockpit.audit.rules.data.model.ExecutionLog;
import org.kockpit.audit.rules.data.model.ResultStatus;
import org.kockpit.audit.rules.data.model.RuleExecution;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ExecutionAudit implements Execution {

  @Setter
  private List<? extends RuleExecution> rules = new ArrayList<>();
  private String executionUuid;
  private Date date;
  private long time;
  @Setter
  private ResultStatus resultStatus;
  private String detail;
  private String message;
  @Setter
  private Long registryId;
  @Setter
  private List<ExecutionLog> executionLogs;

  @Getter
  @Setter
  private List<String> warningRuleCodes = new ArrayList<>();

  @Getter
  @Setter
  private List<String> warningRuleCodeMessages = new ArrayList<>();

  @Getter
  @Setter
  private String errorRuleCode;

  @Getter
  @Setter
  private String errorActionCode;
  @Getter
  @Setter
  private String errorPredicateCode;

  public ExecutionAudit() {}

  public ExecutionAudit(ExecutionResult executionResult) {
    executionUuid =
        ObjectUtils.defaultIfNull(executionResult.getExecutionId(), UUID.randomUUID().toString());
    this.date = new Date(executionResult.getStartTimestamp());
    this.time = executionResult.getTimeInMs();

    Throwable throwable = executionResult.getThrowable();
    if (throwable != null) {
      message = throwable.getMessage();
      detail = ExceptionUtils.getStackTrace(executionResult.getThrowable());
    }
  }

  @Override
  public String getExecutionUuid() {
    return executionUuid;
  }

  @Override
  public List<? extends RuleExecution> getRules() {
    return rules;
  }

  @Override
  public Long getRegistryId() {
    return registryId;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public String getDetail() {
    return detail;
  }

  @Override
  public long getTime() {
    return time;
  }

  @Override
  public ResultStatus getResultStatus() {
    return resultStatus;
  }

  @Override
  public Date getDate() {
    return date;
  }

  @Override
  public List<ExecutionLog> getExecutionLogs() {
    return executionLogs;
  }

}
