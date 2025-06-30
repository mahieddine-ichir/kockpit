package org.kockpit.rules.audit;

import org.kockpit.rules.execution.StepExecution;
import org.kockpit.rules.execution.StepType;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.kockpit.audit.rules.data.model.ActionPredicateExecution;
import org.kockpit.audit.rules.data.model.ResultStatus;
import org.kockpit.audit.rules.data.model.TypeAP;

import java.util.Date;

import static org.kockpit.audit.rules.data.model.ResultStatus.ERROR;
import static org.kockpit.audit.rules.data.model.ResultStatus.WARNING;

public class ActionPredicateAudit implements ActionPredicateExecution {
  private long id;
  private TypeAP type;
  @Setter
  private String code;
  @Setter
  private String name;
  @Setter
  private int position;

  @Getter
  private final RuleExecutionAudit ruleExecution;
  private String message;
  private String detail;
  private final long time;
  private final ResultStatus resultStatus;
  private final Date date;

  public ActionPredicateAudit(RuleExecutionAudit ruleEDT, StepExecution stepExecution) {
    this.ruleExecution = ruleEDT;
    time = stepExecution.getTimeInMs();
    date = new Date(stepExecution.getStartTimestamp());

    Throwable throwable = stepExecution.getThrowable();
    if (StepType.ACTION.equals(stepExecution.getType())) {
      type = TypeAP.ACTION;
      if (stepExecution.isExecutionError()) {
        resultStatus = ERROR;
      } else if (stepExecution.isExecutionWarning()) {
        resultStatus = WARNING;
        ruleEDT.setResultStatus(WARNING);
      } else {
        resultStatus = ResultStatus.VALID;
      }
    } else {
      type = TypeAP.PREDICATE;
      if (stepExecution.isExecutionError()) {
        resultStatus = ERROR;
      } else if (stepExecution.isExecutionWarning()) {
        resultStatus = WARNING;
        ruleEDT.setResultStatus(WARNING);
      } else if (stepExecution.isResult()) {
        resultStatus = ResultStatus.CONDITION_TRUE;
      } else {
        resultStatus = ResultStatus.CONDITION_FALSE;
      }
    }
    if (throwable != null) {
      message = throwable.getMessage();
      detail = ExceptionUtils.getStackTrace(throwable);
    }
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public TypeAP getTypeAP() {
    return type;
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
  public int getPosition() {
    return position;
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
}
