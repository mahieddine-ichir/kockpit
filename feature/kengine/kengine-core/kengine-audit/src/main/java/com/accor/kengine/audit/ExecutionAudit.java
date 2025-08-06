package com.accor.kengine.audit;

import com.accor.kengine.audit.model.Execution;
import com.accor.kengine.audit.model.ExecutionLog;
import com.accor.kengine.audit.model.ResultStatus;
import com.accor.kengine.audit.model.RuleExecution;
import com.accor.kengine.execution.ExecutionResult;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class ExecutionAudit implements Execution {
  private List<? extends RuleExecution> rules = new ArrayList<>();
  private String executionUuid;
  private Date date;
  private long time;
  private ResultStatus resultStatus;
  private String detail;
  private String message;
  private Long registryId;
  private List<ExecutionLog> executionLogs;

  private List<String> warningRuleCodes = new ArrayList<>();

  private List<String> warningRuleCodeMessages = new ArrayList<>();

  private String errorRuleCode;

  private String errorActionCode;
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

  public void setRules(List<? extends RuleExecution> rules) {
    this.rules = rules;
  }

  public void setResultStatus(ResultStatus resultStatus) {
    this.resultStatus = resultStatus;
  }

  public void setRegistryId(Long registryId) {
    this.registryId = registryId;
  }

  public List<String> getWarningRuleCodes() {
    return warningRuleCodes;
  }

  public String getErrorRuleCode() {
    return errorRuleCode;
  }

  public void setWarningRuleCodes(List<String> warningRuleCodes) {
    this.warningRuleCodes = warningRuleCodes;
  }

  public void setErrorRuleCode(String errorRule) {
    this.errorRuleCode = errorRule;
  }

  public List<String> getWarningRuleCodeMessages() {
    return warningRuleCodeMessages;
  }

  public void setWarningRuleCodeMessages(List<String> warningRuleCodeMessages) {
    this.warningRuleCodeMessages = warningRuleCodeMessages;
  }

  public String getErrorActionCode() {
    return errorActionCode;
  }

  public void setErrorActionCode(String errorActionCode) {
    this.errorActionCode = errorActionCode;
  }

  public void setErrorPredicateCode(String errorPredicateCode) {
    this.errorPredicateCode = errorPredicateCode;
  }

  public String getErrorPredicateCode() {
    return errorPredicateCode;
  }

  @Override
  public List<ExecutionLog> getExecutionLogs() {
    return executionLogs;
  }

  public void setExecutionLogs(List<ExecutionLog> executionLogs) {
    this.executionLogs = executionLogs;
  }
}
