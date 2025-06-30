package org.kockpit.rules;

import lombok.Setter;

@Setter
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

  @Override
  public String getDocumentation() {
    return documentation;
  }

  @Override
  public String toString() {
    return code;
  }
}
