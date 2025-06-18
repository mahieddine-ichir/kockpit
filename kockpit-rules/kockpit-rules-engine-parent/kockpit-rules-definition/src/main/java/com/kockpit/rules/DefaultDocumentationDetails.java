package com.kockpit.rules;

public class DefaultDocumentationDetails implements DocumentationDetails {
  private String code;

  private String documentation;

  public DefaultDocumentationDetails(String code, String documentation) {
    this.code = code;
    this.documentation = documentation;
  }

  @Override
  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  @Override
  public String getDocumentation() {
    return documentation;
  }

  public void setDocumentation(String documentation) {
    this.documentation = documentation;
  }

  @Override
  public String toString() {
    return code;
  }
}
