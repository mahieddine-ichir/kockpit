package org.kockpit.rules.audit;

import lombok.Getter;
import lombok.Setter;
import org.kockpit.audit.rules.data.model.ActionPredicateExecution;
import org.kockpit.audit.rules.data.model.Execution;
import org.kockpit.audit.rules.data.model.ResultStatus;
import org.kockpit.audit.rules.data.model.RuleExecution;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.kockpit.audit.rules.data.model.ResultStatus.ERROR;
import static org.kockpit.audit.rules.data.model.ResultStatus.VALID;

@Setter
@Getter
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
      ExecutionAudit executionAudit, org.kockpit.rules.execution.RuleExecution ruleExecution) {
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
}
