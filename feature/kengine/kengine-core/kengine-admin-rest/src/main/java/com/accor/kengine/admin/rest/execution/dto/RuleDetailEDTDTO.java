package com.accor.kengine.admin.rest.execution.dto;

public class RuleDetailEDTDTO extends AbstractEDTDTO {

  private String actionPredicate;
  private boolean condition;
  private String name;
  private String detail;

  public String getActionPredicate() {
    return actionPredicate;
  }

  public void setActionPredicate(String actionPredicate) {
    this.actionPredicate = actionPredicate;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public boolean isCondition() {
    return condition;
  }

  public void setCondition(boolean condidtion) {
    this.condition = condidtion;
  }
}
