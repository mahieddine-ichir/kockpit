package com.accor.wcp.audit.module.kengine.flow.serializer;

public class RuleEDTDTO extends AbstractEDTDTO {

  private String name;
  private String detail;

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
}
