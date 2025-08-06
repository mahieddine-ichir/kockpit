package com.accor.kengine;

public class SimpleDetail {

  private String code;

  private String name;

  private String description;

  public SimpleDetail(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public SimpleDetail(String code, String name, String description) {
    this.code = code;
    this.name = name;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
