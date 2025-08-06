package com.accor.wcp.audit.module.kengine.flow.serializer;

import com.accor.kengine.audit.model.ActionPredicateExecution;
import com.accor.kengine.audit.model.ResultStatus;
import com.accor.kengine.audit.model.TypeAP;
import java.util.Date;
import lombok.Data;

@Data
public class ActionPredicateAudit implements ActionPredicateExecution {

  private long id;

  private TypeAP typeAP;

  private String code;

  private String name;

  private int position;

  private String message;

  private String detail;

  private long time;

  private ResultStatus resultStatus;

  private Date date;

  public ActionPredicateAudit() {}

  public ActionPredicateAudit(ActionPredicateExecution predicateExecution) {
    typeAP = predicateExecution.getTypeAP();
    code = predicateExecution.getCode();
    name = predicateExecution.getName();
    position = predicateExecution.getPosition();
    message = predicateExecution.getMessage();
    detail = predicateExecution.getDetail();
    time = predicateExecution.getTime();
    resultStatus = predicateExecution.getResultStatus();
    date = predicateExecution.getDate();
  }
}
