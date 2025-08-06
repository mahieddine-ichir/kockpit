package com.accor.kengine.audit;

import static com.accor.kengine.audit.model.ResultStatus.ERROR;
import static com.accor.kengine.audit.model.ResultStatus.VALID;

import com.accor.kengine.audit.model.ActionPredicateExecution;
import com.accor.kengine.audit.model.Execution;
import com.accor.kengine.audit.model.ResultStatus;
import com.accor.kengine.audit.model.RuleExecution;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class RuleExecutionAudit implements RuleExecution {

  private String message;

  private Date date;

  private long id;

  private String detail;

  private long time;

  private ResultStatus resultStatus;

  private Execution execution;

  private String code;

  private String name;

  private int position;

  private List<ActionPredicateExecution> actionPredicates = new LinkedList<>();

  private boolean skipped;

  public RuleExecutionAudit(
      ExecutionAudit executionAudit, com.accor.kengine.execution.RuleExecution ruleExecution) {
    execution = executionAudit;
    time = ruleExecution.getTimeInMs();
    date = new Date(ruleExecution.getStartTimestamp());
    if (ruleExecution.isError()) {
      resultStatus = ERROR;
    } else {
      resultStatus = VALID;
    }
    this.skipped = ruleExecution.getRuleNodeExecution().isSkipped();
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
  public long getId() {
    return id;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String getName() {
    return name;
  }

  //    @Override
  public Execution getExecution() {
    return execution;
  }

  @Override
  public List<ActionPredicateExecution> getActionPredicates() {
    return actionPredicates;
  }

  @Override
  public int getPosition() {
    return position;
  }

  @Override
  public boolean isSkipped() {
    return skipped;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public void setResultStatus(ResultStatus resultStatus) {
    this.resultStatus = resultStatus;
  }

  public void setSkipped(boolean skipped) {
    this.skipped = skipped;
  }
}
