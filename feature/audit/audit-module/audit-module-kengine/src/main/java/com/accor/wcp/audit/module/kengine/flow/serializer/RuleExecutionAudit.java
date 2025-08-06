package com.accor.wcp.audit.module.kengine.flow.serializer;

import static java.util.stream.Collectors.toList;

import com.accor.kengine.audit.model.ResultStatus;
import com.accor.kengine.audit.model.RuleExecution;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;

@Data
public class RuleExecutionAudit implements RuleExecution {

  private String message;

  private Date date;

  private long id;

  private String detail;

  private long time;

  private ResultStatus resultStatus;

  private String code;

  private String name;

  private int position;

  private List<ActionPredicateAudit> actionPredicates = new LinkedList<>();

  private boolean skipped;

  public RuleExecutionAudit() {}

  public RuleExecutionAudit(RuleExecution ruleExecution) {
    time = ruleExecution.getTime();
    date = ruleExecution.getDate();
    resultStatus = ruleExecution.getResultStatus();
    code = ruleExecution.getCode();
    name = ruleExecution.getName();
    detail = ruleExecution.getDetail();

    this.actionPredicates =
        ruleExecution.getActionPredicates().stream()
            .map(ActionPredicateAudit::new)
            .collect(toList());
    this.skipped = ruleExecution.isSkipped();
  }
}
