package org.kockpit.rules.audit.flow.serializer;

import java.util.Date;
import lombok.Data;
import org.kockpit.audit.rules.data.model.ActionPredicateExecution;
import org.kockpit.audit.rules.data.model.ResultStatus;
import org.kockpit.audit.rules.data.model.TypeAP;

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
