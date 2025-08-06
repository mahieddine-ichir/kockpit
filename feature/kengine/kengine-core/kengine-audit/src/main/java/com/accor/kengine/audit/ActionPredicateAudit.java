package com.accor.kengine.audit;

import static com.accor.kengine.audit.model.ResultStatus.ERROR;
import static com.accor.kengine.audit.model.ResultStatus.WARNING;

import com.accor.kengine.audit.model.ActionPredicateExecution;
import com.accor.kengine.audit.model.ResultStatus;
import com.accor.kengine.audit.model.RuleExecution;
import com.accor.kengine.audit.model.TypeAP;
import com.accor.kengine.execution.StepExecution;
import com.accor.kengine.execution.StepType;
import java.util.Date;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class ActionPredicateAudit implements ActionPredicateExecution {
  private long id;
  private TypeAP type;
  private String code;
  private String name;
  private int position;
  private RuleExecutionAudit ruleExecution;
  private String message;
  private String detail;
  private long time;
  private ResultStatus resultStatus;
  private Date date;

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

  //    @Override
  public RuleExecution getRuleExecution() {
    return ruleExecution;
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

  public void setCode(String code) {
    this.code = code;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPosition(int position) {
    this.position = position;
  }
}
